package com.mbank.service;

import java.util.List;

import com.mbank.dto.TransactionDTO;

public interface TransactionService {

	List<TransactionDTO> getAllTransactionsByAccountNumber(String accountNumber);
	void sendBankStatementByEmail(String accountNumber);

}
