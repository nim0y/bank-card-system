package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequestMapping("/api/v1/user/cards")
@RequiredArgsConstructor
@Tag(name = "User Card API", description = "Управление своими картами")
public class UserCardController {

    private final CardService cardService;
    private final CardMapper cardMapper;

    @GetMapping
    @Operation(summary = "Просмотр своих карт (поиск по номеру/имени + пагинация + баланс)")
    public ResponseEntity<Page<CardResponseDTO>> getMyCards(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CardStatus status,
            @ParameterObject Pageable pageable) {

        Page<CardResponseDTO> cards = cardService.getMyCards(principal.getName(), search, status, pageable)
                .map(cardMapper::toDto);
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Перевод денежных средств")
    public ResponseEntity<String> transfer(
            Principal principal,
            @Valid @RequestBody TransferRequest request) {

        cardService.transferBetweenOwnCards(
                principal.getName(),
                request.getFromCardId(),
                request.getToCardId(),
                request.getAmount()
        );
        return ResponseEntity.ok("Перевод успешно выполнен");
    }

    @PatchMapping("/{id}/block")
    @Operation(summary = "Запрос на блокировку своей карты")
    public ResponseEntity<Void> lockMyCard(Principal principal, @PathVariable Long id) {
        cardService.lockCard(principal.getName(), id);
        return ResponseEntity.ok().build();
    }
}