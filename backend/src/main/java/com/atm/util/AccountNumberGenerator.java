package com.atm.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates 12-digit numeric account numbers. Uniqueness is enforced by the
 * DB unique constraint; the service retries on the (rare) collision.
 */
@Component
public class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate() {
        // first digit 1-9 to keep a fixed 12-digit length
        StringBuilder sb = new StringBuilder(12);
        sb.append(1 + RANDOM.nextInt(9));
        for (int i = 0; i < 11; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
