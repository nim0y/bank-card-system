package com.example.bankcards.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransferRequest {
    @NotNull
    private Long fromCardId;
    @NotNull
    private Long toCardId;
    @Positive
    @NotNull
    private BigDecimal amount;
}