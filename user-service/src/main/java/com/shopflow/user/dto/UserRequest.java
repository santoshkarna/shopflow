package com.shopflow.user.dto;

import java.time.LocalDate;

public record UserRequest(
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
