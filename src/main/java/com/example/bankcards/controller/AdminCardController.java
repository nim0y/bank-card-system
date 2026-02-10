package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.AdminCardService;
import com.example.bankcards.util.CardMapper;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/cards")
@RequiredArgsConstructor
@Tag(name = "Admin Card API", description = "Администрирование карт всех пользователей")
public class AdminCardController {

    private final AdminCardService adminService;
    private final CardMapper cardMapper;

    @GetMapping
    @Operation(summary = "Просмотр всех карт в системе (только для админа)")
    public ResponseEntity<Page<CardResponseDTO>> getAllCards(@ParameterObject Pageable pageable) {
        Page<CardResponseDTO> cards = adminService.getAllCards(pageable)
                .map(cardMapper::toDto);
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    @Operation(summary = "Создание карт")
    public ResponseEntity<CardResponseDTO> createCard(@Valid @RequestBody CreateCardRequest req) {
        Card card = adminService.createCard(req.getUserId(), req.getCardNumber(), req.getInitialBalance());
        return ResponseEntity.status(HttpStatus.CREATED).body(cardMapper.toDto(card));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Смена статуса карты")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestParam CardStatus status) {
        adminService.updateCardStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление карты администратором")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        adminService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}