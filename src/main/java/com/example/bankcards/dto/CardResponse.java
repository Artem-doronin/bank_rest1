package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardResponse {
    private Long id;
    private String maskedCardNumber; // Например: "****1234"
    private LocalDateTime expiryDate;
    private String cardType;
    private BigDecimal balance;
    private CardStatus status;
    private Long userId;
}