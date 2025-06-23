package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserStatusUpdateRequest;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API для управления пользователями")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class UserController {

    private final UserService userService;


    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Изменить статус Пользователя (ACTIVE, BLOCKED, DELETED")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusUpdateRequest request
    ) {
        userService.updateUserStatus(userId,request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasAnyRole( 'ADMIN')")
    @Operation(summary = "Получить всех пользователей ", description = "Доступно только администраторам")
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }
}
