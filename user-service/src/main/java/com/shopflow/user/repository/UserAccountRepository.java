package com.shopflow.user.repository;

import com.shopflow.user.dto.UserResponse;
import com.shopflow.user.domain.Role;
import com.shopflow.user.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    List<UserAccount> findByRoleOrderByIdAsc(Role role);

    Optional<UserAccount> findByEmail(String Email);

    List<UserResponse> findByRole(Role role);
}
