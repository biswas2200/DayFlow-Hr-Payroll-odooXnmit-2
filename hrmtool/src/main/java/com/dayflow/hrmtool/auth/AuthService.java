package com.dayflow.hrmtool.auth;

import com.dayflow.hrmtool.auth.dto.ChangePasswordRequest;
import com.dayflow.hrmtool.auth.dto.LoginRequest;
import com.dayflow.hrmtool.auth.dto.LoginResponse;
import com.dayflow.hrmtool.auth.dto.RefreshRequest;
import com.dayflow.hrmtool.common.AuthenticationException;
import com.dayflow.hrmtool.common.BusinessException;
import com.dayflow.hrmtool.common.ResourceNotFoundException;
import com.dayflow.hrmtool.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByLoginIdOrEmail(request.getLoginId(), request.getLoginId())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user, user.getId(), user.getRole().name());
        String refreshTokenString = jwtTokenProvider.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .userId(user.getId())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .role(user.getRole())
                .mustChangePassword(user.isMustChangePassword())
                .userId(user.getId())
                .employeeId(user.getEmployeeId())
                .build();
    }

    public LoginResponse refreshToken(RefreshRequest request) {
        String tokenString = request.getRefreshToken();
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Refresh token expired or revoked");
        }

        AppUser user = appUserRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", refreshToken.getUserId()));

        // Rotate tokens
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user, user.getId(), user.getRole().name());
        String newRefreshTokenString = jwtTokenProvider.generateRefreshToken(user);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenString)
                .userId(user.getId())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenString)
                .role(user.getRole())
                .mustChangePassword(user.isMustChangePassword())
                .userId(user.getId())
                .employeeId(user.getEmployeeId())
                .build();
    }

    public void logout(Long userId) {
        refreshTokenRepository.revokeByUserId(userId);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid current password");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("New passwords do not match");
        }

        validatePasswordStrength(request.getNewPassword());

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        appUserRepository.save(user);
        refreshTokenRepository.revokeByUserId(userId);
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessException("Password must be at least 8 characters long");
        }
        boolean hasUpper = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        if (!hasUpper || !hasDigit || !hasSpecial) {
            throw new BusinessException("Password must contain at least one uppercase letter, one digit, and one special character");
        }
    }
}
