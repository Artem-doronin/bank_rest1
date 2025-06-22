package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CardCreateRequest {

    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String cardNumber;

    @NotNull(message = "Expiration date is required (MM/YY)")
    private LocalDateTime expiryDate;

    @NotNull(message = "User ID is required")
    private Long userId;

}
