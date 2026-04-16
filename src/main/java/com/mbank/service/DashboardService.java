package com.mbank.service;

import com.mbank.dto.AccountResponse;
import com.mbank.dto.UserResponse;

public interface DashboardService {
    UserResponse getUserDetails(String accountNumber);
    AccountResponse getAccountDetails(String accountNumber);
}