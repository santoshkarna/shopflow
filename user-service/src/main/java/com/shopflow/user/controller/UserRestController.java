package com.shopflow.user.controller;

import com.shopflow.user.dto.UserResponse;
import com.shopflow.user.exceptions.UserNotFoundException;
import com.shopflow.user.domain.Role;
import com.shopflow.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Service", description = "User Management APIs")
public class UserRestController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable @Positive Long id) {
        return userService.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @GetMapping
    public List<UserResponse> getAllUsers(@RequestParam Role role) {
        return userService.findByRole(role);
    }
}
