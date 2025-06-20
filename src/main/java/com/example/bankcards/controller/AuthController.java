package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для аутентификации пользователей")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Вход пользователя",
            description = "Аутентификация пользователя по логину и паролю, возвращает JWT токен",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешная аутентификация",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
            }
    )

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }
}
