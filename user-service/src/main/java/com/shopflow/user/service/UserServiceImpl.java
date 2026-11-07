package com.shopflow.user.service;

import com.shopflow.user.dto.UserRequest;
import com.shopflow.user.dto.UserResponse;
import com.shopflow.user.dto.UserUpdateRequest;
import com.shopflow.user.exceptions.UserNotFoundException;
import com.shopflow.user.mapper.UserMapper;
import com.shopflow.user.model.Role;
import com.shopflow.user.model.User;
import com.shopflow.user.repository.RoleRepository;
import com.shopflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserResponse createUser(UserRequest request) {
        log.info("Creating user {}", request);
        User user = UserMapper.toEntity(request);
        Role role = roleRepository.findByName(request.roleName());
        if(role != null) {
            buildRoles(user, role);
        }
        User savedUser = userRepository.save(user);
        log.info("Saved user {}", savedUser);
        return UserMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("Updating user {}", id);
        Optional<User> optionalUser = userRepository.findById(id);
        if(optionalUser.isEmpty()){
            log.error("User with id {} not found", id);
            throw new UserNotFoundException("User with id " + id + " not found");
        }
        User user = UserMapper.toEntity(optionalUser.get(), request);
        Role role = roleRepository.findByName(request.roleName());
        if(role != null) {
            buildRoles(user, role);
        }
        User updatedUser = userRepository.save(user);
        log.info("Updated user {}", updatedUser);
        return UserMapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user {}", id);
        boolean userExists = userRepository.existsById(id);
        if(!userExists){
            log.error("User with id {} not found", id);
            throw new UserNotFoundException("User with id " + id + " not found");
        }
        userRepository.deleteById(id);
        log.info("Deleted user {}", id);
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.info("Getting user {}", id);
        Optional<User> optionalUser = userRepository.findById(id);
        if(optionalUser.isEmpty()){
            log.error("User with id {} not found", id);
            throw new UserNotFoundException("User with id " + id + " not found");
        }
        log.info("Found user {}", optionalUser.get());
        return optionalUser.map(UserMapper::toResponse).orElse(null);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Getting all users");
        List<User> users = userRepository.findAll();
        if(users.isEmpty()){
            log.error("User list is empty");
            throw new UserNotFoundException("User list is empty");
        }
        log.info("Found {} users", users.size());
        return users.stream().map(UserMapper::toResponse).toList();
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        log.info("Getting user by username {}", username);
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if(optionalUser.isEmpty()){
            log.error("User with username {} not found", username);
            throw new UserNotFoundException("User with username " + username + " not found");
        }
        log.info("Found user {}", optionalUser.get());
        return optionalUser.map(UserMapper::toResponse).orElse(null);
    }

    private void buildRoles(User user, Role role) {
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
    }
}
