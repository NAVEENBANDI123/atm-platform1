package com.atm.entity;

/**
 * Two-stage approval status used by both card and loan applications.
 *
 * <ol>
 *   <li>{@link #PENDING}     - customer has just submitted, awaiting officer review.</li>
 *   <li>{@link #UNDER_REVIEW}- officer (CARD_OFFICER / LOAN_OFFICER) has reviewed and recommended approval.</li>
 *   <li>{@link #APPROVED}    - SUPER_ADMIN approved.</li>
 *   <li>{@link #REJECTED}    - rejected at any stage with a reason.</li>
 * </ol>
 */
public enum ApplicationStatus {
    PENDING,
    UNDER_REVIEW,
    APPROVED,
    REJECTED
}
