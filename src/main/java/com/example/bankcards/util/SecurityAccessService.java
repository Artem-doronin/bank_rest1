package com.example.bankcards.util;

import com.example.bankcards.exception.UserNotAuthenticatedException;
import com.example.bankcards.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SecurityAccessService {

    public void checkUserAccess(Long cardUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.error("Пользователь не аутентифицирован");
            throw new UserNotAuthenticatedException("Пользователь не аутентифицирован");
        }

        String currentUserRole = auth.getAuthorities().toString();
        Long currentUserId = getCurrentUserId();

        if (currentUserRole.contains("ROLE_ADMIN")) {
            log.debug("Пользователь с ID {} имеет роль ADMIN, доступ разрешён", currentUserId);
            return;
        }

        if (!currentUserId.equals(cardUserId)) {
            log.warn("Пользователь с ID {} пытается получить доступ к данным пользователя с ID {}", currentUserId, cardUserId);
            throw new AccessDeniedException("Нет прав для доступа к этой карте");
        }
        log.debug("Пользователь с ID {} имеет доступ", currentUserId);
    }

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.error("Пользователь не аутентифицирован");
            throw new UserNotAuthenticatedException("Пользователь не аутентифицирован");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            Long id = ((CustomUserDetails) principal).getId();
            log.debug("Текущий пользователь аутентифицирован с ID {}", id);
            return id;
        } else {
            log.error("Принципал не является экземпляром CustomUserDetails");
            throw new UserNotAuthenticatedException("Не удалось определить пользователя");
        }
    }
}
