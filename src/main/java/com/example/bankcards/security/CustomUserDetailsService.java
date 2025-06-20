package com.example.bankcards.security;

import com.example.bankcards.entity.User;
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
    public UserDetails loadUserByUsername(String username)
    {
        log.info("Попытка загрузить пользователя по username: {}", username);
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
            Integer a = user.getRoles().size();



            log.info("Пользователь найден: id={}, username={}", user.getId(), user.getUsername());
            return CustomUserDetails.fromUserEntity(user);
        } catch (UsernameNotFoundException ex) {
            log.warn("Пользователь не найден: {}", username);
            throw ex;
        } catch (Exception ex) {
            log.error("Ошибка при загрузке пользователя с username {}: {}", username, ex.getMessage(), ex);
            throw ex;
        }
    }

}
