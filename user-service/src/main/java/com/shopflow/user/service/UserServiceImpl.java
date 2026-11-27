package com.shopflow.user.service;

import com.shopflow.user.dto.UserResponse;
import com.shopflow.user.domain.Role;
import com.shopflow.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public Optional<UserResponse> findById(Long id) {
        return userAccountRepository.findById(id).map(UserResponse::from);
    }

    @Override
    public List<UserResponse> findByRole(Role role) {
        return userAccountRepository.findByRoleOrderByIdAsc(role).stream().map(UserResponse::from).toList();
    }
}
