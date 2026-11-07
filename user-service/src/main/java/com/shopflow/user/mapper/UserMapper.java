package com.shopflow.user.mapper;

import com.shopflow.user.dto.UserRequest;
import com.shopflow.user.dto.UserResponse;
import com.shopflow.user.dto.UserUpdateRequest;
import com.shopflow.user.model.Role;
import com.shopflow.user.model.User;

import java.time.LocalDate;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(UserRequest userRequest) {
        return User.builder()
                .firstName(userRequest.firstName())
                .lastName(userRequest.lastName())
                .username(userRequest.username())
                .password(userRequest.password())
                .gender(userRequest.gender())
                .email(userRequest.email())
                .phone(userRequest.phone())
                .dob(userRequest.dob())
                .build();
    }


    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getPhone(),
                user.getDob(),
                user.getGender(),
                user.getRoles().stream().map(Role::getName).findFirst().orElse(null)
        );
    }


    public static User toEntity(User user, UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest.firstName() != null) user.setFirstName(userUpdateRequest.firstName());
        if (userUpdateRequest.lastName() != null) user.setLastName(userUpdateRequest.lastName());
        if (userUpdateRequest.password() != null) user.setPassword(userUpdateRequest.password());
        if (userUpdateRequest.email() != null) user.setEmail(userUpdateRequest.email());
        if (userUpdateRequest.phone() != null) user.setPhone(userUpdateRequest.phone());
        if (userUpdateRequest.dob() != null) user.setDob(userUpdateRequest.dob());
        return user;
    }
}
