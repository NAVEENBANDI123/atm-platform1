package com.atm.entity;

/**
 * Approval lifecycle on a {@link CustomerProfile}.
 *
 * <p>A new customer registration starts as {@link #PENDING_APPROVAL} and is
 * blocked from logging in.  An ACCOUNTANT or SUPER_ADMIN may approve it, at
 * which point a customer ID and account number are issued and the
 * underlying user becomes {@link UserStatus#ACTIVE}.</p>
 */
public enum CustomerStatus {
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    SUSPENDED
}
