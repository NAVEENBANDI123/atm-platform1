package com.atm.service;

import com.atm.dto.request.NomineeCreateRequest;
import com.atm.dto.response.NomineeResponse;

import java.util.List;

public interface NomineeService {

    NomineeResponse add(String username, NomineeCreateRequest request);

    List<NomineeResponse> list(String username);

    void remove(String username, Long nomineeId);
}
