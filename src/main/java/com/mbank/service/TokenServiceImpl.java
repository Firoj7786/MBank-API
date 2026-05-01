package com.mbank.service;

import static org.springframework.security.core.userdetails.User.withUsername;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mbank.exception.InvalidTokenException;
import com.mbank.repository.UserRepository;
import com.mbank.util.ApiMessages;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

import jakarta.annotation.PreDestroy;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Stateless JWT service.
 *
 * Key design decisions:
 * - JWTs are validated purely by cryptographic signature + expiry; no DB lookup per request.
 * - Logout / forced revocation uses an in-memory denylist keyed by JTI (JWT ID).
 *   Entries are automatically evicted once the token's natural expiry passes, so
 *   the denylist stays small even under heavy traffic.
 * - The Token entity and TokenRepository are no longer needed and can be removed
 *   from the project entirely.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration; // milliseconds

    private final UserRepository userRepository;

    /**
     * In-memory denylist: JTI → expiry epoch-ms.
     * ConcurrentHashMap is safe for concurrent reads/writes without explicit locking.
     * Entries are cleaned up by a background scheduler so memory stays bounded.
     */
    private final Map<String, Long> tokenDenylist = new ConcurrentHashMap<>();

    private final ScheduledExecutorService denylistCleaner =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "jwt-denylist-cleaner");
                t.setDaemon(true);
                return t;
            });

    {
        // Purge expired denylist entries every 10 minutes so the map never grows unbounded.
        denylistCleaner.scheduleAtFixedRate(this::purgeExpiredDenylistEntries, 10, 10, TimeUnit.MINUTES);
    }

    // -------------------------------------------------------------------------
    // Token generation
    // -------------------------------------------------------------------------

    @Override
    public String generateToken(UserDetails userDetails) {
        log.info("Generating token for user: {}", userDetails.getUsername());
        return doGenerateToken(userDetails, new Date(System.currentTimeMillis() + expiration));
    }

    @Override
    public String generateToken(UserDetails userDetails, Date expiry) {
        log.info("Generating token for user: {}", userDetails.getUsername());
        return doGenerateToken(userDetails, expiry);
    }

    private String doGenerateToken(UserDetails userDetails, Date expiry) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                // JTI (JWT ID) is a unique identifier per token — used for denylist lookup.
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    // -------------------------------------------------------------------------
    // Token validation — pure crypto + denylist; zero DB calls
    // -------------------------------------------------------------------------

    @Override
    public void validateToken(String token) throws InvalidTokenException {
        // This parses and verifies signature + expiry. Throws on any problem.
        val claims = getAllClaimsFromToken(token);

        // Check denylist (logout / forced invalidation).
        if (tokenDenylist.containsKey(claims.getId())) {
            throw new InvalidTokenException(ApiMessages.TOKEN_NOT_FOUND_ERROR.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Token invalidation (logout)
    // -------------------------------------------------------------------------

    @Override
    public void invalidateToken(String token) {
        try {
            val claims = getAllClaimsFromToken(token);
            val jti = claims.getId();
            val expiryMs = claims.getExpiration().getTime();

            tokenDenylist.put(jti, expiryMs);
            log.info("Token invalidated (JTI: {})", jti);

        } catch (InvalidTokenException e) {
            // Token is already expired / malformed — nothing useful to denylist.
            log.debug("Attempted to invalidate an already-invalid token: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Claims extraction helpers
    // -------------------------------------------------------------------------

    @Override
    public String getUsernameFromToken(String token) throws InvalidTokenException {
        return getClaimFromToken(token, Claims::getSubject);
    }

    @Override
    public Date getExpirationDateFromToken(String token) throws InvalidTokenException {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    @Override
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver)
            throws InvalidTokenException {
        return claimsResolver.apply(getAllClaimsFromToken(token));
    }

    private Claims getAllClaimsFromToken(String token) throws InvalidTokenException {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException(ApiMessages.TOKEN_EXPIRED_ERROR.getMessage());
        } catch (UnsupportedJwtException e) {
            throw new InvalidTokenException(ApiMessages.TOKEN_UNSUPPORTED_ERROR.getMessage());
        } catch (MalformedJwtException e) {
            throw new InvalidTokenException(ApiMessages.TOKEN_MALFORMED_ERROR.getMessage());
        } catch (SignatureException e) {
            throw new InvalidTokenException(ApiMessages.TOKEN_SIGNATURE_INVALID_ERROR.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException(ApiMessages.TOKEN_EMPTY_ERROR.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // UserDetailsService
    // -------------------------------------------------------------------------

    @Override
    public UserDetails loadUserByUsername(String accountNumber) throws UsernameNotFoundException {
        val user = userRepository.findByAccountAccountNumber(accountNumber)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format(ApiMessages.USER_NOT_FOUND_BY_ACCOUNT.getMessage(), accountNumber)));

        return withUsername(accountNumber)
                .password(user.getPassword())
                .build();
    }

    // -------------------------------------------------------------------------
    // Denylist maintenance
    // -------------------------------------------------------------------------

    private void purgeExpiredDenylistEntries() {
        val now = System.currentTimeMillis();
        val removed = new int[]{0};
        tokenDenylist.entrySet().removeIf(entry -> {
            if (entry.getValue() < now) {
                removed[0]++;
                return true;
            }
            return false;
        });
        if (removed[0] > 0) {
            log.debug("Purged {} expired entries from JWT denylist", removed[0]);
        }
    }

    @PreDestroy
    public void shutdownCleaner() {
        denylistCleaner.shutdownNow();
    }
}