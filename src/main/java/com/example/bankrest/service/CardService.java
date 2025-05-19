package com.example.bankrest.service;

import com.example.bankrest.dto.CardDto;
import com.example.bankrest.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Сервис для работы с банковскими картами:
 *  – создание/удаление/блокировка/смена статуса
 *  – просмотр списка карт пользователя (с фильтром и пагинацией)
 *  – получение детальной информации о карте (с расшифровкой номера)
 *  – перевод между картами пользователя
 */
public interface CardService {

    CardDto createCard(CardDto dto);

    CardDto updateStatus(Long cardId, String status);

    CardDto requestBlock(Long cardId, String username);

    CardDto getCardDetails(Long cardId, String username);

    Page<CardDto> listUserCards(
            Long userId,
            CardStatus status,
            String maskedNumberPart,
            Pageable pageable
    );

    void transfer(Long fromCardId, Long toCardId, BigDecimal amount, String username);

    void deleteCard(Long cardId);
}
