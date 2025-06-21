package com.example.bankcards.dto;

import com.example.bankcards.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateRequest {
    @NotNull(message = "Status must be provided")
    private UserStatus status;
}
