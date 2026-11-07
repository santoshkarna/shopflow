package com.shopflow.user.dto;

import java.time.LocalDate;

public record UserResponse(
    Long id,
    String firstName,
    String lastName,
    String username,
    String password,
    String email,
    String phone,
    LocalDate dob,
    String gender,
    String roleName
) {
}
