package com.atm.service.impl;

import com.atm.config.AppProperties;
import com.atm.entity.EmailOutbox;
import com.atm.entity.EmailStatus;
import com.atm.repository.EmailOutboxRepository;
import com.atm.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Outbox-pattern email service.
 *
 * <p>{@link #send} renders the template and persists a row in
 * {@code email_outbox} with status {@code QUEUED}; an asynchronous worker
 * (kicked off both on-write and via {@link Scheduled}) drains the outbox
 * via SMTP. This pattern means the calling business transaction never
 * blocks on SMTP and emails survive a transient mail-server outage.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailOutboxRepository outboxRepository;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Override
    public void send(String to, String subject, String template, Map<String, Object> variables) {
        send(to, List.of(), subject, template, variables);
    }

    @Override
    @Transactional
    public void send(String to, List<String> cc, String subject, String template,
                     Map<String, Object> variables) {
        String body = renderTemplate(template, variables);
        EmailOutbox row = EmailOutbox.builder()
                .toAddress(to)
                .ccAddresses(cc == null || cc.isEmpty() ? null : String.join(",", cc))
                .subject(subject)
                .body(body)
                .template(template)
                .status(EmailStatus.QUEUED)
                .attempts(0)
                .build();
        outboxRepository.save(row);
        // Best-effort immediate dispatch in the background; the @Scheduled
        // flushOutbox() acts as a retry safety net.
        triggerAsyncFlush();
    }

    @Async
    public void triggerAsyncFlush() {
        try {
            flushOutbox();
        } catch (Exception ex) {
            log.warn("Async email flush failed: {}", ex.getMessage());
        }
    }

    @Override
    @Scheduled(fixedDelay = 60_000L, initialDelay = 30_000L)
    @Transactional
    public void flushOutbox() {
        if (!appProperties.getMail().isEnabled()) {
            return;
        }
        List<EmailOutbox> batch = outboxRepository.findByStatusOrderByIdAsc(
                EmailStatus.QUEUED, PageRequest.of(0, 50));
        if (batch.isEmpty()) {
            return;
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("No JavaMailSender configured; {} queued email(s) remain pending.", batch.size());
            return;
        }
        for (EmailOutbox row : batch) {
            try {
                MimeMessage mime = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mime, false, StandardCharsets.UTF_8.name());
                helper.setFrom(new InternetAddress(
                        appProperties.getMail().getFrom(),
                        appProperties.getMail().getFromName()));
                helper.setTo(row.getToAddress());
                if (row.getCcAddresses() != null && !row.getCcAddresses().isBlank()) {
                    helper.setCc(row.getCcAddresses().split(","));
                }
                helper.setSubject(row.getSubject());
                helper.setText(row.getBody(), true);
                mailSender.send(mime);
                row.setStatus(EmailStatus.SENT);
                row.setSentAt(LocalDateTime.now());
                row.setLastError(null);
                outboxRepository.save(row);
            } catch (MailException | MessagingException | UnsupportedEncodingException ex) {
                row.setAttempts(row.getAttempts() + 1);
                row.setLastError(truncate(ex.getMessage(), 1000));
                if (row.getAttempts() >= 5) {
                    row.setStatus(EmailStatus.FAILED);
                    log.error("Email permanently failed after {} attempts: id={}, to={}",
                            row.getAttempts(), row.getId(), row.getToAddress());
                }
                outboxRepository.save(row);
            }
        }
    }

    private String renderTemplate(String template, Map<String, Object> variables) {
        try {
            Context ctx = new Context();
            if (variables != null) {
                variables.forEach(ctx::setVariable);
            }
            return templateEngine.process(template, ctx);
        } catch (RuntimeException ex) {
            log.warn("Falling back to plain-text template '{}': {}", template, ex.getMessage());
            // Plain-text fallback so a missing template never breaks flow.
            StringBuilder sb = new StringBuilder("Hello,\n\n");
            if (variables != null) {
                variables.forEach((k, v) -> sb.append(k).append(": ").append(v).append('\n'));
            }
            sb.append("\nRegards,\nATM Platform");
            return sb.toString();
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}
