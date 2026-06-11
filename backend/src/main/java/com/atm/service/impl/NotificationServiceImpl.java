package com.atm.service.impl;

import com.atm.common.PageResponse;
import com.atm.dto.response.NotificationResponse;
import com.atm.entity.Notification;
import com.atm.entity.User;
import com.atm.exception.ResourceNotFoundException;
import com.atm.mapper.DomainMapper;
import com.atm.repository.NotificationRepository;
import com.atm.repository.UserRepository;
import com.atm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final DomainMapper mapper;

    @Override
    @Transactional
    public void push(User user, String title, String body) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .title(title)
                .body(body)
                .read(false)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return PageResponse.from(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                        .map(mapper::toNotificationResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Override
    @Transactional
    public int markAllRead(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.markAllAsRead(user.getId());
    }
}
