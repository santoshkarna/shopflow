package com.shopflow.user.dto;

public record AuthRequest(
        String username,
        String password
) {
}
