package com.atm.service;

import com.atm.dto.request.TransferRequest;
import com.atm.dto.response.AccountResponse;
import com.atm.dto.response.BalanceResponse;
import com.atm.dto.response.CustomerDashboardResponse;
import com.atm.dto.response.TransactionResponse;

public interface AccountService {

    /** Customer self-view of their primary account.  Balance is masked. */
    AccountResponse getMyAccount(String username);

    /** Customer dashboard payload (no balance). */
    CustomerDashboardResponse getMyDashboard(String username);

    /** Reveal the actual balance (called when the user clicks "Show Balance"). */
    BalanceResponse getMyBalance(String username);

    /** Customer-initiated transfer with daily-limit + balance + beneficiary checks. */
    TransactionResponse transfer(String username, TransferRequest request);
}
