package com.dayflow.hrmtool.auth;

import com.dayflow.hrmtool.auth.dto.ChangePasswordRequest;
import com.dayflow.hrmtool.auth.dto.LoginRequest;
import com.dayflow.hrmtool.auth.dto.LoginResponse;
import com.dayflow.hrmtool.auth.dto.RefreshRequest;
import com.dayflow.hrmtool.common.AuthenticationException;
import com.dayflow.hrmtool.common.BusinessException;
import com.dayflow.hrmtool.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService — covers login, refresh token rotation, logout, and password change.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @InjectMocks private AuthService authService;

    private AppUser activeUser;

    @BeforeEach
    void setUp() {
        // Build an AppUser that simulates a saved entity with id=1
        activeUser = AppUser.builder()
                .loginId("DAEMPL20240001")
                .email("john@dayflow.com")
                .passwordHash("$2a$bcryptHash")
                .role(Role.EMPLOYEE)
                .mustChangePassword(false)
                .employeeId(1L)
                .build();
        // Simulate a persisted entity by using reflection or overriding id
        // Since BaseEntity has private id set by JPA, we mock the whole thing at test level
        // Just stub anyLong() for userId in generateAccessToken
        when(jwtTokenProvider.generateAccessToken(any(), anyLong(), anyString()))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any()))
                .thenReturn("refresh-token");
    }

    // =========== LOGIN TESTS ===========

    @Test
    void login_withValidCredentials_returnsTokenPair() {
        LoginRequest req = new LoginRequest("DAEMPL20240001", "Password1!");
        when(appUserRepository.findByLoginIdOrEmail(anyString(), anyString()))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Password1!", "$2a$bcryptHash")).thenReturn(true);
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LoginResponse response = authService.login(req);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(response.isMustChangePassword()).isFalse();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_withWrongPassword_throwsBusinessException() {
        LoginRequest req = new LoginRequest("DAEMPL20240001", "WrongPassword!");
        when(appUserRepository.findByLoginIdOrEmail(anyString(), anyString()))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("WrongPassword!", "$2a$bcryptHash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_withUnknownUser_throwsBusinessException() {
        LoginRequest req = new LoginRequest("UNKNOWN123", "Password1!");
        when(appUserRepository.findByLoginIdOrEmail(anyString(), anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_canUseEmailInsteadOfLoginId() {
        LoginRequest req = new LoginRequest("john@dayflow.com", "Password1!");
        when(appUserRepository.findByLoginIdOrEmail("john@dayflow.com", "john@dayflow.com"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LoginResponse resp = authService.login(req);
        assertThat(resp.getAccessToken()).isNotBlank();
    }

    @Test
    void login_mustChangePassword_flaggedInResponse() {
        activeUser.setMustChangePassword(true);
        LoginRequest req = new LoginRequest("DAEMPL20240001", "TempPass1!");
        when(appUserRepository.findByLoginIdOrEmail(anyString(), anyString()))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LoginResponse resp = authService.login(req);
        assertThat(resp.isMustChangePassword()).isTrue();
    }

    // =========== REFRESH TOKEN TESTS ===========

    @Test
    void refreshToken_withValidToken_rotatesTokens() {
        RefreshToken storedToken = RefreshToken.builder()
                .token("old-refresh-token")
                .userId(1L)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("old-refresh-token"))
                .thenReturn(Optional.of(storedToken));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LoginResponse resp = authService.refreshToken(new RefreshRequest("old-refresh-token"));

        assertThat(resp.getAccessToken()).isEqualTo("access-token");
        assertThat(resp.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(storedToken.isRevoked()).isTrue(); // old token revoked
    }

    @Test
    void refreshToken_withExpiredToken_throwsBusinessException() {
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired-token")
                .userId(1L)
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS)) // expired
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.refreshToken(new RefreshRequest("expired-token")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void refreshToken_withRevokedToken_throwsBusinessException() {
        RefreshToken revokedToken = RefreshToken.builder()
                .token("revoked-token")
                .userId(1L)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(true) // revoked
                .build();
        when(refreshTokenRepository.findByToken("revoked-token"))
                .thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> authService.refreshToken(new RefreshRequest("revoked-token")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void refreshToken_withUnknownToken_throwsBusinessException() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(new RefreshRequest("unknown")))
                .isInstanceOf(BusinessException.class);
    }

    // =========== LOGOUT TESTS ===========

    @Test
    void logout_revokesAllRefreshTokens() {
        authService.logout(1L);
        verify(refreshTokenRepository).revokeByUserId(1L);
    }

    // =========== CHANGE PASSWORD TESTS ===========

    @Test
    void changePassword_withCorrectCurrentPassword_updatesHash() {
        ChangePasswordRequest req = new ChangePasswordRequest("OldPass1!", "NewPass2@", "NewPass2@");
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("OldPass1!", "$2a$bcryptHash")).thenReturn(true);
        when(passwordEncoder.encode("NewPass2@")).thenReturn("$2a$newHash");
        when(appUserRepository.save(any())).thenReturn(activeUser);

        authService.changePassword(1L, req);

        assertThat(activeUser.getPasswordHash()).isEqualTo("$2a$newHash");
        assertThat(activeUser.isMustChangePassword()).isFalse();
        verify(refreshTokenRepository).revokeByUserId(1L);
    }

    @Test
    void changePassword_withWrongCurrentPassword_throwsBusinessException() {
        ChangePasswordRequest req = new ChangePasswordRequest("WrongOld!", "NewPass2@", "NewPass2@");
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("WrongOld!", "$2a$bcryptHash")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid current password");
    }

    @Test
    void changePassword_withMismatchedNewPasswords_throwsBusinessException() {
        ChangePasswordRequest req = new ChangePasswordRequest("OldPass1!", "NewPass2@", "Different3#");
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("OldPass1!", "$2a$bcryptHash")).thenReturn(true);

        assertThatThrownBy(() -> authService.changePassword(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("do not match");
    }

    @Test
    void changePassword_withWeakPassword_throwsBusinessException() {
        ChangePasswordRequest req = new ChangePasswordRequest("OldPass1!", "weak", "weak");
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("OldPass1!", "$2a$bcryptHash")).thenReturn(true);

        assertThatThrownBy(() -> authService.changePassword(1L, req))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void changePassword_withNoUppercase_throwsBusinessException() {
        ChangePasswordRequest req = new ChangePasswordRequest("OldPass1!", "nouppercase1!", "nouppercase1!");
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("OldPass1!", "$2a$bcryptHash")).thenReturn(true);

        assertThatThrownBy(() -> authService.changePassword(1L, req))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void changePassword_withNoSpecialChar_throwsBusinessException() {
        ChangePasswordRequest req = new ChangePasswordRequest("OldPass1!", "NoSpecial123", "NoSpecial123");
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("OldPass1!", "$2a$bcryptHash")).thenReturn(true);

        assertThatThrownBy(() -> authService.changePassword(1L, req))
                .isInstanceOf(BusinessException.class);
    }
}
