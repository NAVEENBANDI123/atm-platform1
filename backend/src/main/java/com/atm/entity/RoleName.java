package com.atm.entity;

/**
 * Application roles.
 *
 * <p>The enterprise upgrade introduces a six-role RBAC model (see
 * {@code docs/08-ROLE-PERMISSION-MATRIX.md}). The original {@code ROLE_ADMIN} and
 * {@code ROLE_CUSTOMER} values are retained for backward compatibility: existing admin
 * accounts keep working and {@code ROLE_ADMIN} is treated as an alias of
 * {@code ROLE_SUPER_ADMIN} during the transition.</p>
 */
public enum RoleName {

    /** Legacy super-user role; alias of {@link #ROLE_SUPER_ADMIN} during transition. */
    ROLE_ADMIN,

    /** Full control: creates employees, final approver for customers/loans/cards, deposits. */
    ROLE_SUPER_ADMIN,

    /** Approves customers, views customer details, reviews transactions, generates reports. */
    ROLE_ACCOUNTANT,

    /** Deposits, assisted withdrawals, views customer accounts. */
    ROLE_CASHIER,

    /** Reviews loan applications and recommends approval/rejection (no final sanction). */
    ROLE_LOAN_OFFICER,

    /** Reviews card requests and recommends approval/rejection (no final issuance). */
    ROLE_CARD_OFFICER,

    /** End customer: self-service banking. */
    ROLE_CUSTOMER
}
