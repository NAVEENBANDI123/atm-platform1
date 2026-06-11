package com.atm.audit;

import com.atm.entity.AuditLog;
import com.atm.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.stream.Collectors;

/**
 * Persists security/business audit events. Runs in its own transaction so an
 * audit failure never rolls back the business operation it describes.
 *
 * <p>Three overloads:
 * <ul>
 *   <li>{@link #record(String, String, String, String)} - simple log line.</li>
 *   <li>{@link #recordWithValues} - includes before/after values.</li>
 *   <li>{@link #record(String, String, String, String, String, String, String)}
 *       - full form used by {@code AuditAspect}.</li>
 * </ul></p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void record(String action, String entityType, String entityId, String details) {
        record(action, entityType, entityId, details, null, null, null);
    }

    public void recordWithValues(String action, String entityType, String entityId,
                                 String details, String oldValue, String newValue) {
        record(action, entityType, entityId, details, oldValue, newValue, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String entityType, String entityId, String details,
                       String oldValue, String newValue, String overrideUsername) {
        try {
            AuditLog entry = AuditLog.builder()
                    .username(overrideUsername != null ? overrideUsername : currentUsername())
                    .userRole(currentRoles())
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(truncate(details, 1000))
                    .oldValue(truncate(oldValue, 2000))
                    .newValue(truncate(newValue, 2000))
                    .ipAddress(currentIp())
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            log.warn("Failed to write audit log for action {}: {}", action, ex.getMessage());
        }
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "anonymous";
        }
        return auth.getName();
    }

    private String currentRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return null;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    private String currentIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            return (forwarded != null && !forwarded.isBlank())
                    ? forwarded.split(",")[0].trim()
                    : request.getRemoteAddr();
        } catch (Exception ex) {
            return null;
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}
