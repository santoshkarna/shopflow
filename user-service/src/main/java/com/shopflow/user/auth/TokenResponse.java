package com.shopflow.user.auth;

public record TokenResponse (
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
