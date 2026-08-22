package com.dayflow.hrmtool.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JwtTokenProvider — covers token generation, validation, and claim extraction.
 */
class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-for-testing-purposes-only-minimum-256-bits-length-required";
    private static final long ACCESS_EXPIRY_MS = 900000L;   // 15 min
    private static final long REFRESH_EXPIRY_MS = 604800000L; // 7 days

    private JwtTokenProvider jwtTokenProvider;
    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiryMs", ACCESS_EXPIRY_MS);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiryMs", REFRESH_EXPIRY_MS);

        testUser = User.builder()
                .username("DAEMPL20240001")
                .password("encodedPassword")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateAccessToken_returnsNonEmptyToken() {
        String token = jwtTokenProvider.generateAccessToken(testUser, 1L, "EMPLOYEE");
        assertThat(token).isNotBlank();
    }

    @Test
    void generateAccessToken_containsUserIdClaim() {
        String token = jwtTokenProvider.generateAccessToken(testUser, 42L, "ADMIN");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        assertThat(userId).isEqualTo(42L);
    }

    @Test
    void generateAccessToken_subjectIsLoginId() {
        String token = jwtTokenProvider.generateAccessToken(testUser, 1L, "EMPLOYEE");
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertThat(username).isEqualTo("DAEMPL20240001");
    }

    @Test
    void generateRefreshToken_returnsNonEmptyToken() {
        String token = jwtTokenProvider.generateRefreshToken(testUser);
        assertThat(token).isNotBlank();
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = jwtTokenProvider.generateAccessToken(testUser, 1L, "EMPLOYEE");
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void validateToken_emptyToken_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    void validateToken_nullToken_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }

    @Test
    void validateToken_tamperedToken_returnsFalse() {
        String token = jwtTokenProvider.generateAccessToken(testUser, 1L, "EMPLOYEE");
        String tampered = token + "tampered";
        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    void getUserIdFromToken_adminRole_returnsCorrectId() {
        String token = jwtTokenProvider.generateAccessToken(testUser, 99L, "ADMIN");
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(99L);
    }

    @Test
    void accessToken_and_refreshToken_areDifferent() {
        String access = jwtTokenProvider.generateAccessToken(testUser, 1L, "EMPLOYEE");
        String refresh = jwtTokenProvider.generateRefreshToken(testUser);
        assertThat(access).isNotEqualTo(refresh);
    }

    @Test
    void generateAccessToken_differentUsers_differentTokens() {
        UserDetails user2 = User.builder()
                .username("XYEMPL20240002")
                .password("pass")
                .authorities(Collections.emptyList())
                .build();
        String t1 = jwtTokenProvider.generateAccessToken(testUser, 1L, "EMPLOYEE");
        String t2 = jwtTokenProvider.generateAccessToken(user2, 2L, "EMPLOYEE");
        assertThat(t1).isNotEqualTo(t2);
    }
}
