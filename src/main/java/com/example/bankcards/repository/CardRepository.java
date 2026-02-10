package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findAllByOwnerUsernameAndOwnerNameContainingIgnoreCase(
            String username,
            String ownerName,
            Pageable pageable
    );

    @Query("SELECT c FROM Card c WHERE c.owner.username = :username " +
            "AND (LOWER(c.ownerName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR c.cardNumber LIKE CONCAT('%', :search, '%'))")
    Page<Card> searchMyCards(@Param("username") String username,
                             @Param("search") String search,
                             Pageable pageable);

    Page<Card> findAllByOwnerUsernameAndStatus(String username, CardStatus status, Pageable pageable);

    Page<Card> findAllByOwnerUsername(String username, Pageable pageable);

    Optional<Card> findByIdAndOwnerUsername(Long id, String username);

    List<Card> findAllByExpiryDateBeforeAndStatus(LocalDate date, CardStatus status);
}