package com.atm.config;

import com.atm.entity.User;
import com.atm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <strong>Self-healing demo credentials.</strong>
 *
 * <p>On every application start, when {@code app.demo.reset-passwords=true},
 * this runner ensures the well-known demo accounts (superadmin, admin,
 * accountant, cashier, cardofficer, loanofficer, jdoe) all log in with the
 * configured demo password (default: {@code password}).</p>
 *
 * <p>It uses the same {@link PasswordEncoder} bean that {@code AuthServiceImpl}
 * uses to verify credentials, so the hash it writes is guaranteed to match.
 * Any drift caused by an old/incorrect seed hash, a database reset, or a
 * forgotten password change is corrected automatically.</p>
 *
 * <p><strong>Production:</strong> set {@code DEMO_RESET_PASSWORDS=false} (or
 * {@code app.demo.reset-passwords: false} in the active profile) to disable
 * this behaviour.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoSeedRunner implements CommandLineRunner {

    private static final List<String> DEMO_USERNAMES = List.of(
            "superadmin", "admin", "accountant", "cashier",
            "cardofficer", "loanofficer", "jdoe"
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public void run(String... args) {
        if (!appProperties.getDemo().isResetPasswords()) {
            log.info("DemoSeedRunner: app.demo.reset-passwords=false, skipping.");
            return;
        }

        String plain = appProperties.getDemo().getDefaultPassword();
        if (plain == null || plain.isBlank()) {
            log.warn("DemoSeedRunner: app.demo.default-password is empty, skipping.");
            return;
        }

        int reset = 0;
        int alreadyValid = 0;
        int missing = 0;
        for (String username : DEMO_USERNAMES) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                missing++;
                continue;
            }
            // Skip if the existing hash already authenticates correctly -
            // that way we don't churn the table on every restart.
            if (user.getPasswordHash() != null
                    && passwordEncoder.matches(plain, user.getPasswordHash())) {
                alreadyValid++;
                continue;
            }
            user.setPasswordHash(passwordEncoder.encode(plain));
            // Clear any failed-attempt lockouts so the user really can log in.
            user.setFailedAttempts(0);
            user.setAccountLocked(false);
            user.setLockTime(null);
            user.setEnabled(true);
            userRepository.save(user);
            reset++;
            log.info("DemoSeedRunner: reset password for demo user '{}'", username);
        }
        log.info("DemoSeedRunner finished. reset={}, already_valid={}, missing={} (out of {} demo users)",
                reset, alreadyValid, missing, DEMO_USERNAMES.size());
        if (reset > 0) {
            log.info("Demo users login with username/{} (e.g. superadmin/{}, accountant/{}, cashier/{}, jdoe/{})",
                    plain, plain, plain, plain, plain);
        }
    }
}
