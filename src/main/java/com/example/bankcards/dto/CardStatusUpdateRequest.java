package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardStatusUpdateRequest {
    @NotNull(message = "Status must be provided")
    private CardStatus status;
}