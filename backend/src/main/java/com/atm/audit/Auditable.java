package com.atm.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method whose successful execution should produce an audit log entry.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /** The action name recorded in the audit log (e.g. "DEPOSIT"). */
    String action();

    /** The entity type the action relates to (e.g. "ACCOUNT"). */
    String entityType() default "";
}
