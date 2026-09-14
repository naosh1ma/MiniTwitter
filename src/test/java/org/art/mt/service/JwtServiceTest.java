package org.art.mt.service;

import org.art.mt.exception.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "test-secret-key-at-least-32-bytes-long-for-hs256");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
    }

    @Test
    void generateToken_thenExtractUsername_roundTrips() {
        String token = jwtService.generateToken("alice");

        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
    }

    @Test
    void isTokenValid_trueForMatchingUsernameAndUnexpiredToken() {
        String token = jwtService.generateToken("alice");

        assertThat(jwtService.isTokenValid(token, "alice")).isTrue();
    }

    @Test
    void isTokenValid_falseForDifferentUsername() {
        String token = jwtService.generateToken("alice");

        assertThat(jwtService.isTokenValid(token, "bob")).isFalse();
    }

    @Test
    void isTokenExpired_falseForFreshToken() {
        String token = jwtService.generateToken("alice");

        assertThat(jwtService.isTokenExpired(token)).isFalse();
    }

    @Test
    void isTokenExpired_throwsInvalidTokenExceptionForAnAlreadyExpiredToken() {
        // jjwt validates the "exp" claim itself during parsing and throws before
        // JwtService ever gets to compare the expiration date - so an expired token
        // never actually produces isTokenExpired() == true in practice, it throws.
        // (isTokenValid() relies on extractUsername() throwing first for the same
        // reason - this expiration check is effectively unreachable dead code.)
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
        String token = jwtService.generateToken("alice");

        assertThatThrownBy(() -> jwtService.isTokenExpired(token))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void extractUsername_throwsInvalidTokenExceptionForGarbageToken() {
        assertThatThrownBy(() -> jwtService.extractUsername("not-a-real-jwt"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void extractUsername_throwsInvalidTokenExceptionWhenSignedWithDifferentKey() {
        JwtService otherKeyService = new JwtService();
        ReflectionTestUtils.setField(otherKeyService, "jwtSecret", "a-completely-different-secret-key-32-bytes-plus");
        ReflectionTestUtils.setField(otherKeyService, "jwtExpiration", 86400000L);
        String tokenSignedByOtherKey = otherKeyService.generateToken("alice");

        assertThatThrownBy(() -> jwtService.extractUsername(tokenSignedByOtherKey))
                .isInstanceOf(InvalidTokenException.class);
    }
}
