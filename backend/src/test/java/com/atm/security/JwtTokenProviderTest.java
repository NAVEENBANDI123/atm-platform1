package com.atm.security;

import com.atm.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        props.getJwt().setSecret("test-secret-key-that-is-at-least-64-bytes-long-for-hmac-sha-signing!!");
        props.getJwt().setAccessTokenExpirationMs(900_000);
        props.getJwt().setRefreshTokenExpirationMs(604_800_000);
        props.getJwt().setIssuer("atm-platform");
        provider = new JwtTokenProvider(props);
    }

    @Test
    void generatedTokenIsValidAndCarriesSubject() {
        String token = provider.generateAccessToken("jdoe", List.of("ROLE_CUSTOMER"));

        assertThat(provider.isValid(token)).isTrue();
        assertThat(provider.getUsername(token)).isEqualTo("jdoe");
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = provider.generateAccessToken("jdoe", List.of("ROLE_CUSTOMER"));
        assertThat(provider.isValid(token + "x")).isFalse();
    }
}
