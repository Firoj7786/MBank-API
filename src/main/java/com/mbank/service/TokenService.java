package com.mbank.service;

import java.util.Date;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.mbank.exception.InvalidTokenException;

import io.jsonwebtoken.Claims;

public interface TokenService extends UserDetailsService {

    public String generateToken(UserDetails userDetails);

    public String generateToken(UserDetails userDetails, Date expiry);

    public String getUsernameFromToken(String token) throws InvalidTokenException;

    public Date getExpirationDateFromToken(String token) throws InvalidTokenException;

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver)
            throws InvalidTokenException;

    /**
     * Validates a JWT token cryptographically (signature + expiry).
     * Also checks the in-memory denylist to reject explicitly invalidated tokens.
     * No database access is performed.
     */
    public void validateToken(String token) throws InvalidTokenException;

    /**
     * Adds the token's JTI to the in-memory denylist until it naturally expires.
     * This replaces the old DB-based saveToken/deleteByToken approach.
     */
    public void invalidateToken(String token);
}