package com.shopflow.product.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Product verifies tokens locally and owns its authorization rules (10.8-T03):
 * browsing is public, changing the catalog is ADMIN-only (403 otherwise), reservations need any verified caller.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String PRODUCTS = "/api/v1/products";

    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info", "/actuator/prometheus").permitAll()   // L41: probes + Prometheus (prod: internal port)                                    // never mask errors as 401
                        .requestMatchers(HttpMethod.GET, PRODUCTS, PRODUCTS + "/**").permitAll()   // browse the catalog
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()  // off in prod
                        .requestMatchers(PRODUCTS, PRODUCTS + "/**").hasRole("ADMIN")              // every other method: admins
                        .anyRequest().authenticated())                                             // reservations (Order relays)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(rolesFromClaim()))
                        .authenticationEntryPoint(SecurityConfig::unauthenticated)
                        .accessDeniedHandler(SecurityConfig::forbidden))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(SecurityConfig::unauthenticated)
                        .accessDeniedHandler(SecurityConfig::forbidden))
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${shopflow.jwt.secret}") String secret, @Value("${shopflow.jwt.issuer}") String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    /** "roles": ["ADMIN"] → authority ROLE_ADMIN, which hasRole("ADMIN") checks. */
    static JwtAuthenticationConverter rolesFromClaim() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    private static void unauthenticated(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException ex) throws IOException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        write(response, request, 401, "unauthenticated", "Authentication required",
                "Send a valid token: Authorization: Bearer <token>.");
    }

    private static void forbidden(HttpServletRequest request, HttpServletResponse response,
                                  AccessDeniedException ex) throws IOException {
        write(response, request, 403, "forbidden", "Forbidden", "Your role does not allow this operation.");
    }

    private static void write(HttpServletResponse response, HttpServletRequest request, int status, String type,
                              String title, String detail) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("{\"type\":\"https://shopflow.example/problems/" + type + "\",\"title\":\"" + title
                + "\",\"status\":" + status + ",\"detail\":\"" + detail + "\",\"instance\":\"" + request.getRequestURI() + "\"}");
    }
}