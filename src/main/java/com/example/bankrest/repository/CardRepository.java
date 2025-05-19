package com.example.bankrest.repository;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.CardStatus;
import com.example.bankrest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findAllByOwner(User owner, Pageable pageable);
    Page<Card> findAllByOwnerAndStatus(User owner, CardStatus status, Pageable pageable);
    Page<Card> findAllByOwnerAndMaskedNumberContains(User owner, String maskedNumber, Pageable pageable);
    Page<Card> findAllByOwnerAndStatusAndMaskedNumberContains(
            User owner,
            CardStatus status,
            String maskedNumber,
            Pageable pageable
    );
}
