package com.atm.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;

/**
 * Centralized generators for human-readable identifiers (customer ID,
 * employee code, card number, loan account number, transaction reference,
 * EMI plan reference).  Customer ID and employee code use database
 * sequences seeded in V4 to guarantee monotonicity across instances.
 */
@Component
@RequiredArgsConstructor
public class IdentifierGenerators {

    private static final SecureRandom RANDOM = new SecureRandom();

    @PersistenceContext
    private final EntityManager entityManager;

    public String nextCustomerId() {
        Number n = (Number) entityManager
                .createNativeQuery("SELECT NEXTVAL('customer_id_seq')")
                .getSingleResult();
        return "CUST" + n.longValue();
    }

    public String nextEmployeeCode() {
        Number n = (Number) entityManager
                .createNativeQuery("SELECT NEXTVAL('employee_code_seq')")
                .getSingleResult();
        return "EMP" + String.format("%05d", n.longValue());
    }

    /** 16-digit card number with last digit a Luhn check digit. */
    public String generateCardNumber(String binPrefix) {
        String prefix = (binPrefix == null || binPrefix.isBlank()) ? "400000" : binPrefix;
        StringBuilder sb = new StringBuilder(prefix);
        while (sb.length() < 15) {
            sb.append(RANDOM.nextInt(10));
        }
        sb.append(luhnCheckDigit(sb.toString()));
        return sb.toString();
    }

    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + last4;
    }

    public String generateLoanAccountNo() {
        StringBuilder sb = new StringBuilder("LN");
        for (int i = 0; i < 12; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    public LocalDate cardExpiryThreeYearsFromNow() {
        return LocalDate.now().plusYears(3).withDayOfMonth(1).plusMonths(1).minusDays(1);
    }

    private int luhnCheckDigit(String partialNumber) {
        int sum = 0;
        boolean alternate = true;
        for (int i = partialNumber.length() - 1; i >= 0; i--) {
            int n = partialNumber.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }
}
