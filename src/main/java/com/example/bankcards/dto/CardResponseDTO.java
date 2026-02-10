package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CardResponseDTO {
    private Long id;
    private String maskedNumber;
    private String ownerName;
    private BigDecimal balance;
    private String status;
    private LocalDate expiryDate;
}