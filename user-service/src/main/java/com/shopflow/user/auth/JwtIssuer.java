package com.shopflow.user.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.shopflow.user.domain.UserAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtIssuer {

    private final MACSigner signer;
    private final String issuer;
    private final Duration ttl;

    public JwtIssuer(
            @Value("${shopflow.jwt.secret}") String secret,
            @Value("${shopflow.jwt.issuer}") String issuer,
            @Value("${shopflow.jwt.ttl}")Duration ttl) throws KeyLengthException {
        this.signer = new MACSigner(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.ttl = ttl;
    }

    public TokenResponse issue(UserAccount account){
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(String.valueOf(account.getId()))
                .claim("roles", List.of(account.getRole().name()))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(ttl)))
                .jwtID(UUID.randomUUID().toString())
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        return new TokenResponse(jwt.serialize(), "Bearer", ttl.toSeconds());
    }
}
