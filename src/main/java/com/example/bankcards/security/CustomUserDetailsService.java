package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserLoadingException;
import com.example.bankcards.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) {
        log.info("Attempting to load user by username: {}", username);
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

            log.info("Пользователь найден: id={}, username={}", user.getId(), user.getUsername());
            return CustomUserDetails.fromUserEntity(user);
        } catch (UsernameNotFoundException ex) {
            log.warn("User not found: {}", username);
            throw ex;
        } catch (Exception ex) {
            log.error("Error loading user with username {}: {}", username, ex.getMessage(), ex);
            throw new UserLoadingException("Failed to load user " + username, ex);
        }
    }
}