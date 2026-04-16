package com.mbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mbank.entity.OtpInfo;

@Repository
public interface OtpInfoRepository extends JpaRepository<OtpInfo, Long> {

    OtpInfo findByAccountNumberAndOtp(String accountNumber, String otp);

    OtpInfo findByAccountNumber(String accountNumber);
}
