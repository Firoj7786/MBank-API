package com.mbank.controller;

import java.util.List;
import java.util.Map;
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
    public ResponseEntity<?> applyLoan(@RequestBody LoanRequest request) {
        loanService.applyLoan(LoggedinUser.getAccountNumber(), request);
        return ResponseEntity.ok(Map.of("msg", "Loan Applied Successfully"));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllLoans() {
        return ResponseEntity.ok(
                loanService.getAllLoans(LoggedinUser.getAccountNumber())
        );
    }

    @GetMapping("/{loanNumber}")
    public ResponseEntity<?> getLoan(@PathVariable String loanNumber) {
        return ResponseEntity.ok(loanService.getLoan(loanNumber));
    }
}