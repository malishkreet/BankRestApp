package com.example.bankrest.service;

import com.example.bankrest.dto.CardDto;
import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.CardStatus;
import com.example.bankrest.entity.User;
import com.example.bankrest.entity.Transfer;
import com.example.bankrest.repository.TransferRepository;
import com.example.bankrest.exception.NotFoundException;
import com.example.bankrest.repository.CardRepository;
import com.example.bankrest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository     cardRepo;
    private final UserRepository     userRepo;
    private final TransferRepository transferRepo;

    @Value("${encryption.secret}")
    private String secret;
    private SecretKeySpec secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = new SecretKeySpec(keyBytes, 0, 16, "AES");
    }

    @Override
    @Transactional
    public CardDto createCard(CardDto dto) {
        User owner = userRepo.findById(dto.getOwnerId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + dto.getOwnerId()));

        String raw = dto.getRawNumber();
        Card c = new Card();
        c.setNumber(encrypt(raw));
        c.setMaskedNumber(mask(raw));
        c.setOwnerName(owner.getUsername());
        c.setExpiryDate(dto.getExpiryDate());
        c.setStatus(dto.getExpiryDate().isBefore(LocalDate.now())
                ? CardStatus.EXPIRED
                : CardStatus.ACTIVE);
        c.setBalance(dto.getBalance());
        c.setOwner(owner);

        return mapToSummaryDto(cardRepo.save(c));
    }

    @Override
    @Transactional
    public CardDto updateStatus(Long cardId, String status) {
        Card c = findCard(cardId);
        c.setStatus(CardStatus.valueOf(status.toUpperCase()));
        return mapToSummaryDto(cardRepo.save(c));
    }

    @Override
    @Transactional
    public CardDto requestBlock(Long cardId, String username) {
        Card c = findCard(cardId);
        if (!c.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Вы можете заблокировать только свои собственные карты");
        }
        c.setStatus(CardStatus.BLOCKED);
        return mapToSummaryDto(cardRepo.save(c));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> listUserCards(Long userId,
                                       CardStatus status,
                                       String numberPart,
                                       Pageable pageable) {
        User owner = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        Page<Card> page;
        boolean hasStatus = status != null;
        boolean hasNumber = numberPart != null && !numberPart.isBlank();

        if (hasStatus && hasNumber) {
            page = cardRepo.findAllByOwnerAndStatusAndMaskedNumberContains(
                    owner, status, numberPart, pageable);
        } else if (hasStatus) {
            page = cardRepo.findAllByOwnerAndStatus(owner, status, pageable);
        } else if (hasNumber) {
            page = cardRepo.findAllByOwnerAndMaskedNumberContains(owner, numberPart, pageable);
        } else {
            page = cardRepo.findAllByOwner(owner, pageable);
        }

        return page.map(this::mapToSummaryDto);
    }

    @Override
    @Transactional
    public void transfer(Long fromCardId,
                         Long toCardId,
                         BigDecimal amount,
                         String username) {

        if (fromCardId.equals(toCardId)) {
            throw new IllegalArgumentException("Невозможно перевести средства на одну и ту же карту");
        }

        Card from = findCard(fromCardId);
        Card to   = findCard(toCardId);

        if (!username.equals(from.getOwner().getUsername()) ||
                !username.equals(to.getOwner().getUsername())) {
            throw new AccessDeniedException("Вы можете осуществлять переводы только между своими собственными картами");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Недостаточно средств");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to  .setBalance(to.getBalance().add(amount));
        cardRepo.save(from);
        cardRepo.save(to);

        Transfer tx = new Transfer();
        tx.setFromCardId(fromCardId);
        tx.setToCardId(toCardId);
        tx.setAmount(amount);
        transferRepo.save(tx);
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepo.existsById(cardId)) {
            throw new NotFoundException("Карта не найдена: " + cardId);
        }
        cardRepo.deleteById(cardId);
    }

    @Override
    @Transactional(readOnly = true)
    public CardDto getCardDetails(Long cardId, String username) {
        Card c = findCard(cardId);
        if (!c.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Вы можете просматривать только свои собственные карты");
        }
        return mapToSummaryDto(c);
    }

    // ——— ВСПОМОГАТЕЛЬНЫЕ ——————————————————————————

    private Card findCard(Long id) {
        return cardRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Карта не найдена: " + id));
    }

    /** Список */
    private CardDto mapToSummaryDto(Card c) {
        CardDto d = new CardDto();
        d.setId(c.getId());
        d.setOwnerId(c.getOwner().getId());
        d.setMaskedNumber(c.getMaskedNumber());
        d.setRawNumber(decrypt(c.getNumber()));
        d.setOwnerName(c.getOwnerName());
        d.setExpiryDate(c.getExpiryDate());
        d.setStatus(c.getStatus().name());
        d.setBalance(c.getBalance());
        return d;
    }

    private String mask(String raw) {
        return "**** **** **** " + raw.substring(raw.length() - 4);
    }

    public String encrypt(String raw) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] enc = cipher.doFinal(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(enc);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при шифровании номера карты", e);
        }
    }

    private String decrypt(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при расшифровке номера карты", e);
        }
    }

}
