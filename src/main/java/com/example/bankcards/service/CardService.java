package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;

    public Page<Card> getMyCards(String username, String search, CardStatus status, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return cardRepository.searchMyCards(username, search, pageable);
        }
        if (status != null) {
            return cardRepository.findAllByOwnerUsernameAndStatus(username, status, pageable);
        }
        return cardRepository.findAllByOwnerUsername(username, pageable);
    }

    @Transactional
    public void transferBetweenOwnCards(String username, Long fromId, Long toId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }

        Card fromCard = cardRepository.findByIdAndOwnerUsername(fromId, username)
                .orElseThrow(() -> new EntityNotFoundException("Карта списания не найдена или не принадлежит вам"));

        Card toCard = cardRepository.findByIdAndOwnerUsername(toId, username)
                .orElseThrow(() -> new EntityNotFoundException("Карта зачисления не найдена или не принадлежит вам"));

        if (fromCard.getStatus() == CardStatus.EXPIRED) {
            throw new IllegalStateException("Карта списания заблокирована или неактивна");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на карте");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    @Transactional
    public void lockCard(String username, Long cardId) {
        Card card = cardRepository.findByIdAndOwnerUsername(cardId, username)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена"));
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }
}