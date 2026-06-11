package com.atm.service;

import com.atm.common.PageResponse;
import com.atm.dto.request.TicketCreateRequest;
import com.atm.dto.request.TicketResolveRequest;
import com.atm.dto.response.TicketResponse;
import com.atm.entity.TicketStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketService {

    TicketResponse create(String username, TicketCreateRequest request);

    List<TicketResponse> mine(String username);

    PageResponse<TicketResponse> listByStatus(TicketStatus status, Pageable pageable);

    TicketResponse resolve(Long id, TicketResolveRequest request, String agentUsername);
}
