package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CardCreateRequest {
    @NotBlank(message = "Cardholder name is required")
    private String cardholderName;

    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String cardNumber;

    @Size(min = 3, max = 4, message = "CVV must be 3 or 4 digits")
    private String cvv;

    @NotBlank(message = "Expiration date is required (MM/YY)")
    private String expiryDate;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Card type is required (VISA, MASTERCARD, etc.)")
    private String cardType;

    private Double balance = 0.0; // Начальный баланс (по умолчанию 0)

}
