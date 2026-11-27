package com.shopflow.user.auth;

import com.shopflow.user.domain.UserAccount;
import com.shopflow.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DUMMY_HASH = "$2b$10$6UF3z0opBS6XkOWR4rbiJeJsV.rO5.9qM5M6.rEBaCViTt8d8mKsm";
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request){
        UserAccount account = userAccountRepository.findByEmail(request.email().trim().toLowerCase(Locale.ROOT)).orElse(null);
        String hash = account != null && account.getPasswordHash() != null ? account.getPasswordHash(): DUMMY_HASH;
        boolean matches = passwordEncoder.matches(request.password(), hash);
        if (!matches || account == null || account.getPasswordHash() == null){
            throw new InvalidCredentialsException();
        }
        return jwtIssuer.issue(account);
    }
}
