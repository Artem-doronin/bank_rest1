package com.example.bankcards.dto;

import com.example.bankcards.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatusUpdateRequest {
    @NotNull(message = "Status must be provided")
    private UserStatus status;
}
