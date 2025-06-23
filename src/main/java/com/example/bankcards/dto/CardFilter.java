package com.example.bankcards.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record CardFilter(
        @Pattern(regexp = "^[0-9]*$", message = "Номер карты должен содержать только цифры")
        String cardNumber
) {
    public boolean hasCardNumberFilter() {
        return cardNumber != null && !cardNumber.isBlank();
    }
}
