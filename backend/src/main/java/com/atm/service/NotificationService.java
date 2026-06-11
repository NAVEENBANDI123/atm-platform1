package com.atm.service;

import com.atm.common.PageResponse;
import com.atm.dto.response.NotificationResponse;
import com.atm.entity.User;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void push(User user, String title, String body);

    PageResponse<NotificationResponse> list(String username, Pageable pageable);

    long unreadCount(String username);

    int markAllRead(String username);
}
