package com.atm.service;

import com.atm.dto.response.UserResponse;

public interface UserService {

    UserResponse getProfile(String username);
}
