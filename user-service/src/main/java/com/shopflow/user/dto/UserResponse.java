package com.shopflow.user.dto;

import com.shopflow.user.domain.Role;
import com.shopflow.user.domain.UserAccount;

public record UserResponse(
    Long id,
    String email,
    String displayName,
    Role role
) {
    public static UserResponse from(UserAccount user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole());
    }
}
