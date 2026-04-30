package com.mbank.controller;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> createFD(@RequestBody FDRequest request) {

        fixedDepositService.createFD(
                LoggedinUser.getAccountNumber(),
                request.getAmount(),
                request.getTenureMonths()      
        );

        return ResponseEntity.ok(Map.of("msg", "FD Created Successfully"));
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

    @PutMapping("/close/{fdNumber}")
    public ResponseEntity<?> closeFD(@PathVariable String fdNumber) {

        fixedDepositService.closeFD(fdNumber);

        return ResponseEntity.ok(Map.of("msg", "FD Closed Successfully"));
    }
}