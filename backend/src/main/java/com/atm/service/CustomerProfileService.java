package com.atm.service;

import com.atm.dto.response.CustomerProfileResponse;

public interface CustomerProfileService {

    CustomerProfileResponse getMyProfile(String username);

    void changePassword(String username, com.atm.dto.request.ChangePasswordRequest request);
}
