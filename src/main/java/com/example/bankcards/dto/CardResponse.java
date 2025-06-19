package com.example.bankcards.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardResponse {
    private Long id;
    private String maskedCardNumber; // Например: "****1234"
    private String cardholderName;
    private String expiryDate;
    private String cardType;
    private Double balance;
    private boolean isActive;
    private Long userId;
}