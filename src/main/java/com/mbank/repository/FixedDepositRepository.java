package com.mbank.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mbank.entity.Account;
import com.mbank.entity.FixedDeposit;

public interface FixedDepositRepository extends JpaRepository<FixedDeposit, Long> {

	List<FixedDeposit> findByAccount_AccountNumber(String accountNumber);

    Optional<FixedDeposit> findByFdNumber(String fdNumber);
}