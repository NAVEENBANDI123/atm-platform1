package com.atm.service;

import com.atm.common.PageResponse;
import com.atm.dto.response.AuditLogResponse;
import org.springframework.data.domain.Pageable;

public interface AuditQueryService {

    PageResponse<AuditLogResponse> list(Pageable pageable);

    PageResponse<AuditLogResponse> listByUsername(String username, Pageable pageable);

    PageResponse<AuditLogResponse> listByEntityType(String entityType, Pageable pageable);
}
