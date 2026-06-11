package com.atm.service;

import com.atm.common.PageResponse;
import com.atm.dto.request.LoanApplyRequest;
import com.atm.dto.request.RejectionRequest;
import com.atm.dto.request.ReviewRequest;
import com.atm.dto.response.LoanAccountResponse;
import com.atm.dto.response.LoanApplicationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LoanService {

    LoanApplicationResponse apply(String username, LoanApplyRequest request);

    List<LoanApplicationResponse> myApplications(String username);

    List<LoanAccountResponse> myLoans(String username);

    PageResponse<LoanApplicationResponse> listPending(Pageable pageable);

    LoanApplicationResponse review(Long applicationId, ReviewRequest request, String reviewerUsername);

    LoanAccountResponse approve(Long applicationId, String approverUsername);

    LoanApplicationResponse reject(Long applicationId, RejectionRequest request, String approverUsername);
}
