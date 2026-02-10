package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardSchedulerService {

    private final CardRepository cardRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkExpiredCards() {
        log.info("Запуск плановой проверки срока действия карт...");

        LocalDate today = LocalDate.now();

        List<Card> expiredCards = cardRepository.findAllByExpiryDateBeforeAndStatus(today, CardStatus.ACTIVE);

        if (!expiredCards.isEmpty()) {
            expiredCards.forEach(card -> card.setStatus(CardStatus.EXPIRED));
            cardRepository.saveAll(expiredCards);
            log.info("Обновлен статус на EXPIRED для {} карт", expiredCards.size());
        }
    }
}
