package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
@Transactional
public class AdminCardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public Page<Card> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    public Card createCard(Long userId, String cardNumber, BigDecimal initialBalance) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Card card = Card.builder()
                .cardNumber(cardNumber)
                .balance(initialBalance)
                .owner(owner)
                .ownerName(owner.getUsername())
                .status(CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().plusYears(4))
                .build();

        return cardRepository.save(card);
    }

    public void updateCardStatus(Long cardId, CardStatus status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена"));
        card.setStatus(status);
        cardRepository.save(card);
    }

    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new EntityNotFoundException("Карта с ID " + id + " не найдена");
        }
        cardRepository.deleteById(id);
    }
}