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

    /** Customer forgot-password: verifies username + mobile, returns a reset token, emails it. */
    String customerForgotPassword(ForgotPasswordRequest request);

    /** Employee forgot-password: verifies username + mobile, returns a reset token, emails it. */
    String employeeForgotPassword(ForgotPasswordRequest request);

    /** Reset password using a reset token issued by either of the forgot-password flows. */
    void resetPassword(ResetPasswordRequest request);
}
