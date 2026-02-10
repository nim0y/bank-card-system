package com.example.bankcards.dto;

import lombok.Data;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class CreateCardRequest {
    @NotNull
    private Long userId;

    @NotNull
    private String cardNumber;

    @NotNull
    @Positive
    private BigDecimal initialBalance;
}
