package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse authenticate(AuthRequest request) {
        log.info("Attempting to authenticate user: {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            log.debug("Authentication successful for user: {}", request.getUsername());

            String token = jwtTokenProvider.generateToken(request.getUsername());
            log.info("JWT token generated for user: {}", request.getUsername());
            log.debug("Generated token: {}", token);
            return AuthResponse.builder()
                    .accessToken(token)
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for user: {} - invalid credentials", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        } catch (AuthenticationException e) {
            log.error("Authentication error for user: {}", request.getUsername(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication for user: {}", request.getUsername(), e);
            throw new RuntimeException("Authentication failed");
        }
    }
}

