package com.atm.security;

import com.atm.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Issues and validates stateless JWT access tokens.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessExpMs;
    private final String issuer;

    public JwtTokenProvider(AppProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
        this.accessExpMs = properties.getJwt().getAccessTokenExpirationMs();
        this.issuer = properties.getJwt().getIssuer();
    }

    public String generateAccessToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessExpMs);
        return Jwts.builder()
                .issuer(issuer)
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public long getAccessTokenExpirationMs() {
        return accessExpMs;
    }

    public String getUsername(String token) {
        return parse(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception ex) {
            log.debug("Invalid JWT: {}", ex.getMessage());
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
