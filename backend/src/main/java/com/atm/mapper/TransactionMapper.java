package com.atm.mapper;

import com.atm.dto.response.TransactionResponse;
import com.atm.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "accountNumber", source = "account.accountNumber")
    @Mapping(target = "counterpartyAccountNumber", source = "counterpartyAccount.accountNumber")
    TransactionResponse toResponse(Transaction transaction);
}
