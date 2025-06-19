package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransferRequest {
    @NotNull(message = "Source card ID is required")
    private Long sourceCardId;

    @NotNull(message = "Destination card ID is required")
    private Long destinationCardId;

    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    private String description; // Опционально: описание перевода
}
