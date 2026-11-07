package com.shopflow.user.dto;

import java.time.LocalDate;

public record UserUpdateRequest(
        String firstName,
        String lastName,
        String password,
        String email,
        String phone,
        LocalDate dob,
        String roleName
) {
}
