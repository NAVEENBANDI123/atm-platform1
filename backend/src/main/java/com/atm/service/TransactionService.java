package com.atm.service;

import com.atm.common.PageResponse;
import com.atm.dto.response.TransactionResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {

    PageResponse<TransactionResponse> getHistory(String username, Pageable pageable);

    List<TransactionResponse> getMiniStatement(String username);

    String exportStatementCsv(String username);
}
