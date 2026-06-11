package com.atm.service;

import com.atm.dto.request.BeneficiaryCreateRequest;
import com.atm.dto.response.BeneficiaryResponse;

import java.util.List;

public interface BeneficiaryService {

    BeneficiaryResponse add(String username, BeneficiaryCreateRequest request);

    List<BeneficiaryResponse> list(String username);

    void remove(String username, Long beneficiaryId);
}
