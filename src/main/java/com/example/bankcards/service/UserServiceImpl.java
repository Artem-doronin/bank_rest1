package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserStatusUpdateRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public void updateUserStatus(Long userId, UserStatusUpdateRequest request) {
        log.info("Обновление статуса пользователя с ID {} на {}", userId, request.getStatus());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден для обновления статуса", userId);
                    return new UserNotFoundException("Пользователь не найден с ID: " + userId);
                });

        user.setStatus(request.getStatus());
        userRepository.save(user);
        log.info("Статус пользователя с ID {} успешно обновлен на {}", userId, request.getStatus());
    }

    @Override
    public List<UserDto> getAllUsers() {
       return userRepository.findAll().stream().map(userMapper::userToUserDto).toList();
    }
}
