package com.atm.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Records an audit entry around any {@link Auditable} method invocation.
 *
 * <p>Direct {@code AuditService.recordWithValues(...)} calls remain the
 * preferred way to log workflow milestones (approvals, transfers, deposits,
 * etc.) because they have access to the meaningful before/after values.
 * This aspect is the safety net for any service method that does not call
 * the audit service explicitly.</p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(com.atm.audit.Auditable)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            // Failures are still worth recording.
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Auditable auditable = signature.getMethod().getAnnotation(Auditable.class);
            auditService.record(
                    auditable.action() + "_FAILED",
                    auditable.entityType(),
                    null,
                    "method=" + signature.getMethod().getName() + " error=" + ex.getMessage());
            throw ex;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditable auditable = method.getAnnotation(Auditable.class);
        if (auditable != null) {
            String args = Arrays.stream(joinPoint.getArgs())
                    .map(a -> a == null ? "null" : a.getClass().getSimpleName())
                    .collect(Collectors.joining(","));
            auditService.record(
                    auditable.action(),
                    auditable.entityType(),
                    extractIdentifier(result),
                    "method=" + method.getName() + " args=[" + args + "]");
        }
        return result;
    }

    private static String extractIdentifier(Object result) {
        if (result == null) return null;
        try {
            // Try common id getters via reflection without forcing a contract.
            Method idGetter = result.getClass().getMethod("id");
            Object id = idGetter.invoke(result);
            return id == null ? null : id.toString();
        } catch (Exception ignored) {
            return null;
        }
    }
}
