package com.dayflow.hrmtool.auth.dto;

import com.dayflow.hrmtool.auth.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Role role;
    private boolean mustChangePassword;
    private Long userId;
    private Long employeeId;
}
