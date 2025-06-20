package com.example.bankcards.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransferResponse {
    private String transactionId;
    private BigDecimal newSourceBalance;
    private BigDecimal newDestinationBalance;
    private String status; // "SUCCESS", "FAILED"
}
