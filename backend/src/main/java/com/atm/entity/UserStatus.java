package com.atm.entity;

/**
 * Lifecycle status on the {@code users} row.  This is independent of the
 * {@link AccountStatus} which lives on the bank-account level.
 *
 * <ul>
 *   <li>{@link #PENDING_APPROVAL} - customer has registered but cannot log in.</li>
 *   <li>{@link #ACTIVE}           - login allowed.</li>
 *   <li>{@link #REJECTED}         - customer registration was rejected.</li>
 *   <li>{@link #SUSPENDED}        - admin temporarily suspended this user.</li>
 *   <li>{@link #DISABLED}         - permanently disabled (employee terminated).</li>
 * </ul>
 */
public enum UserStatus {
    PENDING_APPROVAL,
    ACTIVE,
    REJECTED,
    SUSPENDED,
    DISABLED
}
