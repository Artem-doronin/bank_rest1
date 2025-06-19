package com.example.bankcards.dto;

import lombok.Data;

@Data
public class TransferResponse {
    private String transactionId;
    private Double newSourceBalance;
    private Double newDestinationBalance;
    private String status; // "SUCCESS", "FAILED"
}
