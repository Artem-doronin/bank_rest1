package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserStatusUpdateRequest;

import java.util.List;

public interface UserService {
    void updateUserStatus(Long userId, UserStatusUpdateRequest request);
    List<UserDto> getAllUsers();
}
