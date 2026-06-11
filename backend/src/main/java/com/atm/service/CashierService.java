package com.atm.service;

import com.atm.dto.request.CashierDepositRequest;
import com.atm.dto.request.CashierWithdrawRequest;
import com.atm.dto.response.AccountResponse;
import com.atm.dto.response.TransactionResponse;

public interface CashierService {

    /** Cashier-side account lookup by number (full balance visible). */
    AccountResponse lookup(String accountNumber);

    TransactionResponse deposit(CashierDepositRequest request, String operatorUsername);

    TransactionResponse withdraw(CashierWithdrawRequest request, String operatorUsername);
}
