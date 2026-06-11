package com.atm.service.impl;

import com.atm.audit.AuditService;
import com.atm.common.PageResponse;
import com.atm.dto.request.CardApplyRequest;
import com.atm.dto.request.RejectionRequest;
import com.atm.dto.request.ReviewRequest;
import com.atm.dto.response.CardApplicationResponse;
import com.atm.dto.response.CardResponse;
import com.atm.entity.Account;
import com.atm.entity.ApplicationStatus;
import com.atm.entity.Card;
import com.atm.entity.CardApplication;
import com.atm.entity.CardStatus;
import com.atm.entity.CardType;
import com.atm.entity.User;
import com.atm.exception.BadRequestException;
import com.atm.exception.ResourceNotFoundException;
import com.atm.mapper.DomainMapper;
import com.atm.repository.AccountRepository;
import com.atm.repository.CardApplicationRepository;
import com.atm.repository.CardRepository;
import com.atm.repository.UserRepository;
import com.atm.service.CardService;
import com.atm.service.EmailService;
import com.atm.service.NotificationService;
import com.atm.util.IdentifierGenerators;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardApplicationRepository applicationRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final IdentifierGenerators identifiers;
    private final AuditService auditService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final DomainMapper mapper;

    @Override
    @Transactional
    public CardApplicationResponse apply(String username, CardApplyRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("You must have an active account before applying for a card"));

        boolean alreadyPending = !applicationRepository.findByUserIdAndStatusIn(
                user.getId(),
                List.of(ApplicationStatus.PENDING, ApplicationStatus.UNDER_REVIEW)).isEmpty();
        if (alreadyPending) {
            throw new BadRequestException("You already have a card application in review");
        }

        CardApplication app = CardApplication.builder()
                .user(user)
                .account(account)
                .cardType(req.cardType())
                .status(ApplicationStatus.PENDING)
                .build();
        applicationRepository.save(app);

        auditService.record("CARD_APPLY", "CARD_APPLICATION", String.valueOf(app.getId()),
                "Customer " + username + " applied for a " + req.cardType() + " card");

        emailService.send(user.getEmail(),
                "Card application received",
                "card-application-submitted",
                Map.of("name", user.getFullName(), "cardType", req.cardType().name()));

        // Notify the card-officer pool
        emailService.send("card.officer@atm.local",
                "New card application from " + user.getUsername(),
                "officer-new-card-request",
                Map.of("name", user.getFullName(), "cardType", req.cardType().name(),
                        "applicationId", app.getId()));
        return mapper.toCardApplicationResponse(app);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardApplicationResponse> myApplications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return applicationRepository.findByUserIdOrderByCreatedAtDesc(
                        user.getId(), Pageable.unpaged()).stream()
                .map(mapper::toCardApplicationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> myCards(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return cardRepository.findByAccountUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(mapper::toCardResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CardApplicationResponse> listPending(Pageable pageable) {
        return PageResponse.from(applicationRepository.findByStatusInOrderByCreatedAtAsc(
                        List.of(ApplicationStatus.PENDING, ApplicationStatus.UNDER_REVIEW), pageable)
                .map(mapper::toCardApplicationResponse));
    }

    @Override
    @Transactional
    public CardApplicationResponse review(Long applicationId, ReviewRequest req, String reviewerUsername) {
        CardApplication app = mustApplication(applicationId);
        if (app.getStatus() != ApplicationStatus.PENDING && app.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new BadRequestException("Application is in status " + app.getStatus()
                    + " and cannot be reviewed");
        }
        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        String previous = app.getStatus().name();
        app.setReviewedBy(reviewer.getId());
        app.setReviewNote(req.note());
        if (req.recommendation() == ReviewRequest.Recommendation.RECOMMEND) {
            app.setStatus(ApplicationStatus.UNDER_REVIEW);
        } else {
            app.setStatus(ApplicationStatus.PENDING);
        }
        applicationRepository.save(app);
        auditService.recordWithValues("CARD_REVIEW", "CARD_APPLICATION", String.valueOf(app.getId()),
                "Reviewed by " + reviewerUsername + ": " + req.recommendation(),
                previous, app.getStatus().name());
        return mapper.toCardApplicationResponse(app);
    }

    @Override
    @Transactional
    public CardResponse approve(Long applicationId, String approverUsername) {
        CardApplication app = mustApplication(applicationId);
        if (app.getStatus() != ApplicationStatus.UNDER_REVIEW && app.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Application status is " + app.getStatus());
        }
        User approver = userRepository.findByUsername(approverUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        // Issue card with unique number
        String number;
        int attempts = 0;
        do {
            String bin = app.getCardType() == CardType.CREDIT ? "550000" : "400000";
            number = identifiers.generateCardNumber(bin);
            attempts++;
            if (attempts > 20) throw new IllegalStateException("Could not allocate card number");
        } while (cardRepository.existsByCardNumber(number));

        LocalDate expiry = identifiers.cardExpiryThreeYearsFromNow();
        Card card = Card.builder()
                .application(app)
                .account(app.getAccount())
                .cardNumber(number)
                .maskedNumber(identifiers.maskCardNumber(number))
                .cardType(app.getCardType())
                .expiryDate(expiry)
                .status(CardStatus.ACTIVE)
                .build();
        cardRepository.save(card);

        String previous = app.getStatus().name();
        app.setStatus(ApplicationStatus.APPROVED);
        app.setApprovedBy(approver.getId());
        app.setApprovedAt(LocalDateTime.now());
        applicationRepository.save(app);

        auditService.recordWithValues("CARD_APPROVE", "CARD", String.valueOf(card.getId()),
                "Approved card application " + app.getId(),
                previous, "APPROVED");

        User customer = app.getUser();
        emailService.send(customer.getEmail(),
                "Your " + app.getCardType().name().toLowerCase() + " card is approved",
                "card-approved",
                Map.of(
                        "name", customer.getFullName(),
                        "cardType", app.getCardType().name(),
                        "maskedNumber", card.getMaskedNumber(),
                        "expiryDate", card.getExpiryDate().toString()));
        notificationService.push(customer, "Card approved",
                "Your " + app.getCardType().name().toLowerCase() + " card "
                        + card.getMaskedNumber() + " is now active.");

        return mapper.toCardResponse(card);
    }

    @Override
    @Transactional
    public CardApplicationResponse reject(Long applicationId, RejectionRequest req, String approverUsername) {
        CardApplication app = mustApplication(applicationId);
        if (app.getStatus() == ApplicationStatus.APPROVED) {
            throw new BadRequestException("Cannot reject an already approved card application");
        }
        User approver = userRepository.findByUsername(approverUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        String previous = app.getStatus().name();
        app.setStatus(ApplicationStatus.REJECTED);
        app.setApprovedBy(approver.getId());
        app.setRejectReason(req.reason());
        app.setRejectedAt(LocalDateTime.now());
        applicationRepository.save(app);

        auditService.recordWithValues("CARD_REJECT", "CARD_APPLICATION", String.valueOf(app.getId()),
                "Rejected: " + req.reason(),
                previous, "REJECTED");

        User customer = app.getUser();
        emailService.send(customer.getEmail(),
                "Your card application was not approved",
                "card-rejected",
                Map.of(
                        "name", customer.getFullName(),
                        "cardType", app.getCardType().name(),
                        "reason", req.reason()));
        notificationService.push(customer, "Card application rejected",
                "Reason: " + req.reason());
        return mapper.toCardApplicationResponse(app);
    }

    private CardApplication mustApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("CardApplication", "id", id));
    }
}
