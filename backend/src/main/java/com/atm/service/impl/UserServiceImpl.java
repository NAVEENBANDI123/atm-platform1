package com.atm.service.impl;

import com.atm.dto.response.UserResponse;
import com.atm.entity.User;
import com.atm.exception.ResourceNotFoundException;
import com.atm.mapper.UserMapper;
import com.atm.repository.UserRepository;
import com.atm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }
}
