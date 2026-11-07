package com.shopflow.user.service;

import com.shopflow.user.dto.UserRequest;
import com.shopflow.user.dto.UserResponse;
import com.shopflow.user.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse getUserByUsername(String username);
}
