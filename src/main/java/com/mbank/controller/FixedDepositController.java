package com.mbank.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mbank.dto.FDRequest;
import com.mbank.dto.FixedDepositResponse;
import com.mbank.service.FixedDepositService;
import com.mbank.util.LoggedinUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fd")
@RequiredArgsConstructor
public class FixedDepositController {

    private final FixedDepositService fixedDepositService;

    @PostMapping("/create")
    public ResponseEntity<String> createFD(@RequestBody FDRequest request) {

        fixedDepositService.createFD(
                LoggedinUser.getAccountNumber(),
                request.getAmount(),
                request.getTenureMonths(),
                request.getInterestRate()
        );

        return ResponseEntity.ok("FD Created Successfully");
    }

    @GetMapping("/all")
    public ResponseEntity<List<FixedDepositResponse>> getAllFDs() {
        return ResponseEntity.ok(
                fixedDepositService.getAllFDs(LoggedinUser.getAccountNumber())
        );
    }

    @GetMapping("/{fdNumber}")
    public ResponseEntity<FixedDepositResponse> getFD(@PathVariable String fdNumber) {
        return ResponseEntity.ok(
                fixedDepositService.getFD(fdNumber)
        );
    }

    @PostMapping("/close/{fdNumber}")
    public ResponseEntity<String> closeFD(@PathVariable String fdNumber) {

        fixedDepositService.closeFD(fdNumber);

        return ResponseEntity.ok("FD Closed Successfully");
    }
}