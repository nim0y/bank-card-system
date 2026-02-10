package com.example.bankcards.service;


import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    @DisplayName("Успешный перевод между своими картами")
    void transfer_Success() {
        String username = "testuser";
        Card fromCard = Card.builder()
                .id(1L).ownerName(username).balance(new BigDecimal("1000.00"))
                .status(CardStatus.ACTIVE).build();
        Card toCard = Card.builder()
                .id(2L).ownerName(username).balance(new BigDecimal("500.00"))
                .status(CardStatus.ACTIVE).build();

        when(cardRepository.findByIdAndOwnerUsername(1L, username)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerUsername(2L, username)).thenReturn(Optional.of(toCard));

        cardService.transferBetweenOwnCards(username, 1L, 2L, new BigDecimal("300.00"));

        assertEquals(new BigDecimal("700.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("800.00"), toCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    @DisplayName("Ошибка перевода: Недостаточно средств")
    void transfer_InsufficientFunds() {
        String username = "testuser";

        Card fromCard = Card.builder()
                .id(1L)
                .balance(new BigDecimal("100.00")) // 100 рублей
                .status(CardStatus.ACTIVE)
                .build();

        Card toCard = Card.builder()
                .id(2L)
                .balance(BigDecimal.ZERO)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findByIdAndOwnerUsername(1L, username)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerUsername(2L, username)).thenReturn(Optional.of(toCard));

        assertThrows(InsufficientFundsException.class, () ->
                cardService.transferBetweenOwnCards(username, 1L, 2L, new BigDecimal("500.00"))
        );
    }

    @Test
    @DisplayName("Ошибка перевода: Попытка списать с чужой карты")
    void transfer_NotOwner() {
        String username = "hacker";
        when(cardRepository.findByIdAndOwnerUsername(1L, username)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                cardService.transferBetweenOwnCards(username, 1L, 2L, new BigDecimal("100.00"))
        );
    }

    @Test
    @DisplayName("Блокировка карты пользователем")
    void lockCard_Success() {
        String username = "testuser";
        Card card = Card.builder().id(1L).status(CardStatus.ACTIVE).build();
        when(cardRepository.findByIdAndOwnerUsername(1L, username)).thenReturn(Optional.of(card));

        cardService.lockCard(username, 1L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("Ошибка перевода: Отрицательная сумма")
    void transfer_NegativeAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                cardService.transferBetweenOwnCards("user", 1L, 2L, new BigDecimal("-50.00"))
        );
    }
}