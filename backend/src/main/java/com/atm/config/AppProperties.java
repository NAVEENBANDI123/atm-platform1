package com.atm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * Strongly-typed binding for the {@code app.*} configuration namespace.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Security security = new Security();
    private final Cors cors = new Cors();
    private final Mail mail = new Mail();
    private final Banking banking = new Banking();
    private final Demo demo = new Demo();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs;
        private long refreshTokenExpirationMs;
        private String issuer;
    }

    @Getter
    @Setter
    public static class Security {
        private int maxFailedAttempts = 5;
        private int lockDurationMinutes = 15;
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins;
    }

    @Getter
    @Setter
    public static class Mail {
        private String from = "no-reply@atm.local";
        private String fromName = "ATM Platform";
        private boolean enabled = true;
        private boolean logToConsole = true;
        private List<String> adminRecipients = List.of("admin@atm.local");
        private List<String> cardOfficerRecipients = List.of("card.officer@atm.local");
        private List<String> loanOfficerRecipients = List.of("loan.officer@atm.local");
    }

    @Getter
    @Setter
    public static class Banking {
        private BigDecimal dailyTransferLimit = new BigDecimal("100000.00");
        private BigDecimal fdInterestRate = new BigDecimal("6.50");
        private BigDecimal rdInterestRate = new BigDecimal("6.00");
        private LoanInterest loanInterest = new LoanInterest();
    }

    @Getter
    @Setter
    public static class LoanInterest {
        private BigDecimal personal  = new BigDecimal("12.50");
        private BigDecimal education = new BigDecimal("9.50");
        private BigDecimal vehicle   = new BigDecimal("10.50");
        private BigDecimal home      = new BigDecimal("8.50");
    }

    @Getter
    @Setter
    public static class Demo {
        /**
         * When true, demo seed users get their password reset to {@link #defaultPassword}
         * on every application start. This is a development convenience so the documented
         * credentials always work. MUST be false in any real deployment.
         */
        private boolean resetPasswords = true;

        /** Plain-text password assigned to all demo users when resetPasswords is true. */
        private String defaultPassword = "password";
    }
}
