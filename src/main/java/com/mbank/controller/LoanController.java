package com.mbank.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mbank.dto.LoanRequest;
import com.mbank.dto.LoanResponse;
import com.mbank.service.LoanService;
import com.mbank.util.LoggedinUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loan")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<LoanResponse> applyLoan(@RequestBody LoanRequest request) {
        return ResponseEntity.ok(
                loanService.applyLoan(LoggedinUser.getAccountNumber(), request)
        );
    }

    @GetMapping
    public ResponseEntity<List<LoanResponse>> getAllLoans() {
        return ResponseEntity.ok(
                loanService.getAllLoans(LoggedinUser.getAccountNumber())
        );
    }

    @GetMapping("/{loanNumber}")
    public ResponseEntity<LoanResponse> getLoan(@PathVariable String loanNumber) {
        return ResponseEntity.ok(
                loanService.getLoan(loanNumber)
        );
    }
}