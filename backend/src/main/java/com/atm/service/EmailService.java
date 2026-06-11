package com.atm.service;

import java.util.List;
import java.util.Map;

/**
 * Asynchronous email pipeline. Every method enqueues an email row into
 * {@code email_outbox} and returns immediately; the actual SMTP send is
 * performed on a background worker thread by {@link EmailService#flushOutbox()}.
 *
 * <p>Templates live under {@code classpath:/email-templates/*.html}.</p>
 */
public interface EmailService {

    /** Renders the named template against the given variables and queues the message. */
    void send(String to, String subject, String template, Map<String, Object> variables);

    /** Same as {@link #send} but with explicit CCs (e.g. notifying admin recipients). */
    void send(String to, List<String> cc, String subject, String template, Map<String, Object> variables);

    /** Background flusher: scans {@code QUEUED} rows and dispatches them via SMTP. */
    void flushOutbox();
}
