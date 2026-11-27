package com.shopflow.user.service;

import com.shopflow.user.dto.UserResponse;
import com.shopflow.user.domain.Role;

import java.util.List;
import java.util.Optional;

public interface UserService {

    Optional<UserResponse> findById(Long id);

    List<UserResponse> findByRole(Role role);
}
