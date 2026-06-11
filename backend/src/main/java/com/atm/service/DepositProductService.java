package com.atm.service;

import com.atm.dto.request.FixedDepositRequest;
import com.atm.dto.request.RecurringDepositRequest;
import com.atm.dto.response.DepositProductResponse;

import java.util.List;

public interface DepositProductService {

    DepositProductResponse openFd(String username, FixedDepositRequest request);

    DepositProductResponse openRd(String username, RecurringDepositRequest request);

    List<DepositProductResponse> myDeposits(String username);
}
