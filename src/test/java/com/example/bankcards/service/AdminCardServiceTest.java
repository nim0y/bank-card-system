package com.example.bankcards.service;


import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminCardService adminCardService;

    @Test
    @DisplayName("Админ: Успешное получение всех карт с пагинацией")
    void getAllCards_ShouldReturnPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Card card = Card.builder().id(1L).cardNumber("1111222233334444").build();
        when(cardRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(card)));

        Page<Card> result = adminCardService.getAllCards(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cardRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Админ: Успешное создание карты для существующего пользователя")
    void createCard_Success() {
        Long userId = 1L;
        String cardNumber = "1234123412341234";
        BigDecimal balance = new BigDecimal("1000.00");
        User user = User.builder().id(userId).username("testuser").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Card createdCard = adminCardService.createCard(userId, cardNumber, balance);

        assertNotNull(createdCard);
        assertEquals(cardNumber, createdCard.getCardNumber());
        assertEquals("testuser", createdCard.getOwnerName());
        assertEquals(CardStatus.ACTIVE, createdCard.getStatus());
        assertNotNull(createdCard.getExpiryDate());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Админ: Ошибка при создании карты для несуществующего пользователя")
    void createCard_UserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                adminCardService.createCard(999L, "1234", BigDecimal.ZERO)
        );
    }

    @Test
    @DisplayName("Админ: Успешная смена статуса карты")
    void updateCardStatus_Success() {
        Long cardId = 1L;
        Card card = Card.builder().id(cardId).status(CardStatus.ACTIVE).build();
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        adminCardService.updateCardStatus(cardId, CardStatus.BLOCKED);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("Админ: Успешное удаление карты")
    void deleteCard_Success() {
        Long cardId = 1L;
        when(cardRepository.existsById(cardId)).thenReturn(true);

        adminCardService.deleteCard(cardId);

        verify(cardRepository).deleteById(cardId);
    }

    @Test
    @DisplayName("Админ: Ошибка при удалении несуществующей карты")
    void deleteCard_NotFound() {
        when(cardRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> adminCardService.deleteCard(1L));
        verify(cardRepository, never()).deleteById(anyLong());
    }
}