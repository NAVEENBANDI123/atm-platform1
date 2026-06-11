package com.atm.service.impl;

import com.atm.audit.AuditService;
import com.atm.dto.request.BeneficiaryCreateRequest;
import com.atm.dto.response.BeneficiaryResponse;
import com.atm.entity.Account;
import com.atm.entity.Beneficiary;
import com.atm.entity.User;
import com.atm.exception.BadRequestException;
import com.atm.exception.DuplicateResourceException;
import com.atm.exception.ResourceNotFoundException;
import com.atm.mapper.DomainMapper;
import com.atm.repository.AccountRepository;
import com.atm.repository.BeneficiaryRepository;
import com.atm.repository.UserRepository;
import com.atm.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository repository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final DomainMapper mapper;

    @Override
    @Transactional
    public BeneficiaryResponse add(String username, BeneficiaryCreateRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account own = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("You need an account before adding a beneficiary"));
        if (req.accountNumber().equals(own.getAccountNumber())) {
            throw new BadRequestException("You cannot add yourself as a beneficiary");
        }
        if (repository.existsByOwnerIdAndAccountNumber(user.getId(), req.accountNumber())) {
            throw new DuplicateResourceException("Beneficiary already exists for this account");
        }
        // "Verify" by checking the target account exists in our bank.
        boolean verified = accountRepository.findByAccountNumber(req.accountNumber()).isPresent();

        Beneficiary b = Beneficiary.builder()
                .owner(user)
                .nickname(req.nickname())
                .accountNumber(req.accountNumber())
                .beneficiaryName(req.beneficiaryName())
                .bankName(req.bankName())
                .ifsc(req.ifsc())
                .verified(verified)
                .build();
        repository.save(b);

        auditService.record("ADD_BENEFICIARY", "BENEFICIARY", String.valueOf(b.getId()),
                "Beneficiary " + req.accountNumber()
                        + (verified ? " (verified)" : " (pending verification)"));
        return mapper.toBeneficiaryResponse(b);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> list(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return repository.findByOwnerIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(mapper::toBeneficiaryResponse)
                .toList();
    }

    @Override
    @Transactional
    public void remove(String username, Long id) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Beneficiary b = repository.findByIdAndOwnerId(id, user.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Beneficiary", "id", id));
        repository.delete(b);
        auditService.record("REMOVE_BENEFICIARY", "BENEFICIARY", String.valueOf(id),
                "Removed beneficiary " + b.getAccountNumber());
    }
}
