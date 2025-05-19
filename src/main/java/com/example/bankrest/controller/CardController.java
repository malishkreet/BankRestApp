package com.example.bankrest.controller;

import com.example.bankrest.dto.CardDto;
import com.example.bankrest.dto.TransferRequest;
import com.example.bankrest.dto.UserDto;
import com.example.bankrest.entity.CardStatus;
import com.example.bankrest.service.CardService;
import com.example.bankrest.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/cards")
public class CardController {
    private final CardService service;
    private final CardService cardService;
    private final UserService userService;

    public CardController(CardService service, CardService cardService, UserService userService) {
        this.service = service;
        this.cardService = cardService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Просмотр карт (Только ADMIN)")
    public ResponseEntity<Page<CardDto>> list(
            @RequestParam Long userId,
            @RequestParam(required = false) CardStatus status,
            // поиск по номеру карты
            @RequestParam(required = false) String numberPart,
            @Parameter(hidden = true) Pageable pageable

    ) {
        Page<CardDto> page = service.listUserCards(userId, status, numberPart, pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping
    @Operation(summary = "Создание карт (только ADMIN)")
    public ResponseEntity<CardDto> createCard(
            @Valid @RequestBody CardDto dto
    ) {
        CardDto created = service.createCard(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Поменять статус карты (Только ADMIN)")
    public ResponseEntity<CardDto> changeStatus(
            @PathVariable Long id,
            @RequestParam CardStatus status
    ) {
        CardDto updated = service.updateStatus(id, status.name());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Переводы между считами (Только USER)")
    public ResponseEntity<Void> transfer(
            @Valid @RequestBody TransferRequest request,
            Principal principal
    ) {
        CardDto fromCard = cardService.getCardDetails(request.getFromCardId(), principal.getName());
        if (CardStatus.valueOf(fromCard.getStatus()) == CardStatus.BLOCKED) {
            throw new AccessDeniedException("Нельзя переводить со заблокированной карты");
        } else if (CardStatus.valueOf(fromCard.getStatus()) == CardStatus.EXPIRED) {
            throw new AccessDeniedException("Ваша карта заморожена, обратитесь к админу");
        }

        CardDto toCard = cardService.getCardDetails(request.getToCardId(), principal.getName());
        if (CardStatus.valueOf(toCard.getStatus()) == CardStatus.BLOCKED) {
            throw new AccessDeniedException("Нельзя переводить на заблокированную карту");
        } else if (CardStatus.valueOf(toCard.getStatus()) == CardStatus.EXPIRED) {
            throw new AccessDeniedException("Нельзя переводить на замороженую карту");
        }
        service.transfer(
                request.getFromCardId(),
                request.getToCardId(),
                request.getAmount(),
                principal.getName()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/block")
    @Operation(summary = "Блокировка карты (Только USER)")
    public ResponseEntity<CardDto> requestBlock(
            @PathVariable Long id,
            Principal principal
    ) {
        // principal.getName() — это username залогиненного пользователя
        CardDto blocked = service.requestBlock(id, principal.getName());
        return ResponseEntity.ok(blocked);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление карт (Только ADMIN)")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long id
    ) {
        service.deleteCard(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/me")
    @Operation(summary = "Список карт пользователя (Только USER)")
    public ResponseEntity<List<CardDto>> getMyCards(
            Principal principal,
            @RequestParam(required = false) String numberPart,
            @ParameterObject
            @PageableDefault(size = 20)
            Pageable pageable
    ) {
        UserDto me = userService.findByUsername(principal.getName());

        List<CardDto> cards = cardService
                .listUserCards(me.getId(),
                        null,
                        numberPart,
                        pageable)
                .getContent();

        return ResponseEntity.ok(cards);
    }


}