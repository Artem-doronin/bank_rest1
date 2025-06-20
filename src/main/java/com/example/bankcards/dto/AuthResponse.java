package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    @Builder.Default
    private String tokenType = "Bearer";
    private String accessToken;
}
