package com.atm.service;

import com.atm.common.PageResponse;
import com.atm.dto.request.RejectionRequest;
import com.atm.dto.response.CustomerProfileResponse;
import com.atm.entity.CustomerStatus;
import org.springframework.data.domain.Pageable;

public interface CustomerOnboardingService {

    PageResponse<CustomerProfileResponse> listByStatus(CustomerStatus status, Pageable pageable);

    CustomerProfileResponse getById(Long profileId);

    CustomerProfileResponse approve(Long profileId, String approverUsername);

    CustomerProfileResponse reject(Long profileId, RejectionRequest request, String approverUsername);
}
