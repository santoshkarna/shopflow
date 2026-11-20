package com.shopflow.user.dto;

import com.shopflow.user.model.Role;
import com.shopflow.user.model.UserAccount;

import java.time.LocalDate;

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
