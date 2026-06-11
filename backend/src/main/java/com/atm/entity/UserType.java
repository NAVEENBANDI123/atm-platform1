package com.atm.entity;

/**
 * Distinguishes a customer login (self-service banking) from an employee
 * login (admin / accountant / cashier / card-officer / loan-officer).
 * Customers and employees are stored in the same {@code users} table but
 * authenticate via separate portals and follow different lifecycles.
 */
public enum UserType {
    CUSTOMER,
    EMPLOYEE
}
