package com.atm.service;

import com.atm.common.PageResponse;
import com.atm.dto.request.CardApplyRequest;
import com.atm.dto.request.RejectionRequest;
import com.atm.dto.request.ReviewRequest;
import com.atm.dto.response.CardApplicationResponse;
import com.atm.dto.response.CardResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CardService {

    CardApplicationResponse apply(String username, CardApplyRequest request);

    List<CardApplicationResponse> myApplications(String username);

    List<CardResponse> myCards(String username);

    PageResponse<CardApplicationResponse> listPending(Pageable pageable);

    CardApplicationResponse review(Long applicationId, ReviewRequest request, String reviewerUsername);

    CardResponse approve(Long applicationId, String approverUsername);

    CardApplicationResponse reject(Long applicationId, RejectionRequest request, String approverUsername);
}
