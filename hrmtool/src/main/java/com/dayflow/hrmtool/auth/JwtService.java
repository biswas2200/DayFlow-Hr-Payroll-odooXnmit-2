package com.dayflow.hrmtool.auth;

public interface JwtService {
    String generateAccessToken(AppUser user);
    String generateRefreshToken(AppUser user);
    boolean isTokenValid(String token);
}
