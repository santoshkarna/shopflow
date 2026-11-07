package com.shopflow.user.dto;

public record AuthResponse(
        String token,
        UserResponse response
) {
}
