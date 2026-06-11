package com.atm.service.impl;

import com.atm.audit.AuditService;
import com.atm.dto.request.NomineeCreateRequest;
import com.atm.dto.response.NomineeResponse;
import com.atm.entity.Account;
import com.atm.entity.Nominee;
import com.atm.entity.User;
import com.atm.exception.BadRequestException;
import com.atm.exception.ResourceNotFoundException;
import com.atm.mapper.DomainMapper;
import com.atm.repository.AccountRepository;
import com.atm.repository.NomineeRepository;
import com.atm.repository.UserRepository;
import com.atm.service.NomineeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NomineeServiceImpl implements NomineeService {

    private final NomineeRepository repository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final DomainMapper mapper;

    @Override
    @Transactional
    public NomineeResponse add(String username, NomineeCreateRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Account not found"));

        BigDecimal totalShare = repository.findByAccountIdOrderByCreatedAtDesc(account.getId()).stream()
                .map(Nominee::getSharePercent)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newShare = req.sharePercent() != null ? req.sharePercent() : new BigDecimal("100");
        if (totalShare.add(newShare).compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException("Total nominee share cannot exceed 100%. Currently allocated: "
                    + totalShare);
        }

        Nominee n = Nominee.builder()
                .account(account)
                .name(req.name())
                .relationship(req.relationship())
                .dateOfBirth(req.dateOfBirth())
                .sharePercent(newShare)
                .build();
        repository.save(n);
        auditService.record("ADD_NOMINEE", "NOMINEE", String.valueOf(n.getId()),
                "Nominee " + req.name() + " added");
        return mapper.toNomineeResponse(n);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NomineeResponse> list(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return repository.findByAccountIdOrderByCreatedAtDesc(account.getId()).stream()
                .map(mapper::toNomineeResponse)
                .toList();
    }

    @Override
    @Transactional
    public void remove(String username, Long id) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        Nominee n = repository.findByIdAndAccountId(id, account.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Nominee", "id", id));
        repository.delete(n);
        auditService.record("REMOVE_NOMINEE", "NOMINEE", String.valueOf(id),
                "Removed nominee " + n.getName());
    }
}
