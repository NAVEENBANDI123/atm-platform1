package com.atm.dto.response;

import com.atm.entity.TransactionStatus;
import com.atm.entity.TransactionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransactionResponse(
        Long id,
        String reference,
        String accountNumber,
        String counterpartyAccountNumber,
        TransactionType type,
        TransactionStatus status,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        LocalDateTime createdAt
) {
}
