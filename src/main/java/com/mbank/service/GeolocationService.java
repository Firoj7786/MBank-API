package com.mbank.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;

import com.mbank.dto.GeolocationResponse;

public interface GeolocationService {

    @Async
    public CompletableFuture<GeolocationResponse> getGeolocation(String ip);
}
