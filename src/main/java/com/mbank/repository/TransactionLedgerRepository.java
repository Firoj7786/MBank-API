package com.mbank.repository;

import com.mbank.entity.TransactionLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionLedgerRepository extends JpaRepository<TransactionLedger, Long> {

    List<TransactionLedger> findByUserIdOrderByCreatedAtDesc(Long userId);
}