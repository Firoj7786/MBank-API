package com.mbank.mapper;

import org.springframework.stereotype.Component;

import com.mbank.dto.TransactionDTO;
import com.mbank.entity.Transaction;

@Component
public class TransactionMapper {

    public TransactionDTO toDto(Transaction transaction) {
        return new TransactionDTO(transaction);
    }

}
