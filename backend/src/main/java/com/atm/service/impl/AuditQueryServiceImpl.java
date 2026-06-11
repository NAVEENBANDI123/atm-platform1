package com.atm.service.impl;

import com.atm.common.PageResponse;
import com.atm.dto.response.AuditLogResponse;
import com.atm.mapper.DomainMapper;
import com.atm.repository.AuditLogRepository;
import com.atm.service.AuditQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditQueryServiceImpl implements AuditQueryService {

    private final AuditLogRepository repository;
    private final DomainMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> list(Pageable pageable) {
        return PageResponse.from(repository.findAllByOrderByCreatedAtDesc(pageable)
                .map(mapper::toAuditLogResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> listByUsername(String username, Pageable pageable) {
        return PageResponse.from(repository.findByUsernameOrderByCreatedAtDesc(username, pageable)
                .map(mapper::toAuditLogResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> listByEntityType(String entityType, Pageable pageable) {
        return PageResponse.from(repository.findByEntityTypeOrderByCreatedAtDesc(entityType, pageable)
                .map(mapper::toAuditLogResponse));
    }
}
