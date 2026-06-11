package com.atm.service.impl;

import com.atm.audit.AuditService;
import com.atm.common.PageResponse;
import com.atm.dto.request.TicketCreateRequest;
import com.atm.dto.request.TicketResolveRequest;
import com.atm.dto.response.TicketResponse;
import com.atm.entity.Complaint;
import com.atm.entity.TicketStatus;
import com.atm.entity.User;
import com.atm.exception.ResourceNotFoundException;
import com.atm.mapper.DomainMapper;
import com.atm.repository.ComplaintRepository;
import com.atm.repository.UserRepository;
import com.atm.service.EmailService;
import com.atm.service.NotificationService;
import com.atm.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final DomainMapper mapper;

    @Override
    @Transactional
    public TicketResponse create(String username, TicketCreateRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Complaint c = Complaint.builder()
                .user(user)
                .subject(req.subject())
                .description(req.description())
                .status(TicketStatus.OPEN)
                .build();
        complaintRepository.save(c);
        auditService.record("CREATE_TICKET", "COMPLAINT", String.valueOf(c.getId()),
                "Subject: " + req.subject());

        emailService.send(user.getEmail(),
                "We've received your support request",
                "ticket-acknowledged",
                Map.of("name", user.getFullName(),
                        "subject", req.subject(),
                        "ticketId", c.getId()));
        return mapper.toTicketResponse(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> mine(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(mapper::toTicketResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> listByStatus(TicketStatus status, Pageable pageable) {
        return PageResponse.from(complaintRepository.findByStatusOrderByCreatedAtAsc(status, pageable)
                .map(mapper::toTicketResponse));
    }

    @Override
    @Transactional
    public TicketResponse resolve(Long id, TicketResolveRequest req, String agentUsername) {
        Complaint c = complaintRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Ticket", "id", id));
        User agent = userRepository.findByUsername(agentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        String previous = c.getStatus().name();
        c.setStatus(req.status());
        c.setAssignedTo(agent.getId());
        if (req.resolution() != null) c.setResolution(req.resolution());
        complaintRepository.save(c);

        auditService.recordWithValues("UPDATE_TICKET", "COMPLAINT", String.valueOf(c.getId()),
                "Updated by " + agentUsername, previous, c.getStatus().name());

        if (c.getStatus() == TicketStatus.RESOLVED || c.getStatus() == TicketStatus.CLOSED) {
            emailService.send(c.getUser().getEmail(),
                    "Update on your support ticket",
                    "ticket-resolved",
                    Map.of("name", c.getUser().getFullName(),
                            "subject", c.getSubject(),
                            "status", c.getStatus().name(),
                            "resolution", c.getResolution() == null ? "" : c.getResolution(),
                            "ticketId", c.getId()));
            notificationService.push(c.getUser(),
                    "Ticket #" + c.getId() + " " + c.getStatus().name(),
                    c.getResolution() == null ? "" : c.getResolution());
        }
        return mapper.toTicketResponse(c);
    }
}
