package com.mbank.service;

import org.springframework.stereotype.Service;

import com.mbank.dto.AccountResponse;
import com.mbank.dto.UserResponse;
import com.mbank.exception.NotFoundException;
import com.mbank.repository.AccountRepository;
import com.mbank.repository.UserRepository;
import com.mbank.util.ApiMessages;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Override
    public UserResponse getUserDetails(String accountNumber) {
        val user = userRepository.findByAccountAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException(
                        String.format(ApiMessages.USER_NOT_FOUND_BY_ACCOUNT.getMessage(), accountNumber)));

        return new UserResponse(user);
    }

    @Override
    public AccountResponse getAccountDetails(String accountNumber) {
        val account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new NotFoundException(String.format(ApiMessages.USER_NOT_FOUND_BY_ACCOUNT.getMessage(), accountNumber));
        }

        return new AccountResponse(account);
    }

}
