package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardStatusUpdateRequest {
    @NotNull(message = "Status must be provided (true/false)")
    private Boolean isActive;
}