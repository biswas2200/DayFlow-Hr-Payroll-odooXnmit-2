package com.dayflow.hrmtool.auth;

import com.dayflow.hrmtool.auth.dto.ChangePasswordRequest;
import com.dayflow.hrmtool.auth.dto.LoginRequest;
import com.dayflow.hrmtool.auth.dto.LoginResponse;
import com.dayflow.hrmtool.auth.dto.RefreshRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication and authorization API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AppUser currentUser) {
        authService.logout(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal AppUser currentUser,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok().build();
    }
}
