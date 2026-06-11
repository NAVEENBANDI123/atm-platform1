package com.atm.service;

import com.atm.dto.request.CustomerRegisterRequest;
import com.atm.dto.request.ForgotPasswordRequest;
import com.atm.dto.request.LoginRequest;
import com.atm.dto.request.RefreshTokenRequest;
import com.atm.dto.request.ResetPasswordRequest;
import com.atm.dto.response.AuthResponse;
import com.atm.dto.response.CustomerRegistrationResponse;

public interface AuthService {

    /** Customer self-service registration. Lands in PENDING_APPROVAL. No login is issued. */
    CustomerRegistrationResponse registerCustomer(CustomerRegisterRequest request);

    /** Login restricted to {@code USER_TYPE = CUSTOMER}. Blocked until APPROVED. */
    AuthResponse customerLogin(LoginRequest request);

    /** Login restricted to {@code USER_TYPE = EMPLOYEE} (any banking role). */
    AuthResponse employeeLogin(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    String forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
