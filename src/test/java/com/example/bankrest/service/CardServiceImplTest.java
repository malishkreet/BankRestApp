package com.example.bankrest.service;

import com.example.bankrest.dto.CardDto;
import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.CardStatus;
import com.example.bankrest.entity.User;
import com.example.bankrest.repository.CardRepository;
import com.example.bankrest.repository.TransferRepository;
import com.example.bankrest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepo;
    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private CardServiceImpl cardService;

    private static final String SECRET = "1234567890123456";

    @BeforeEach
    void init() throws Exception {
        java.lang.reflect.Field field = CardServiceImpl.class.getDeclaredField("secret");
        field.setAccessible(true);
        field.set(cardService, SECRET);
        cardService.init();
    }

    @Test
    void createCardSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        // prepare DTO
        com.example.bankrest.dto.CardDto dto = new com.example.bankrest.dto.CardDto();
        dto.setOwnerId(1L);
        dto.setRawNumber("1111222233334444");
        dto.setExpiryDate(LocalDate.now().plusYears(1));
        dto.setBalance(new BigDecimal("1000"));

        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        when(cardRepo.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        CardDto result = cardService.createCard(dto);

        verify(cardRepo).save(captor.capture());
        Card saved = captor.getValue();

        assertThat(saved.getMaskedNumber()).isEqualTo("**** **** **** 4444");
        assertThat(saved.getOwnerName()).isEqualTo("alice");
        assertThat(result.getMaskedNumber()).isEqualTo("**** **** **** 4444");
        assertThat(result.getStatus()).isEqualTo(CardStatus.ACTIVE.name());
    }

    @Test
    void transferInsufficientFundsThrows() {
        Card from = new Card();
        from.setId(1L);
        from.setBalance(new BigDecimal("50"));
        User owner = new User(); owner.setUsername("Alice"); from.setOwner(owner);
        Card to = new Card(); to.setId(2L); to.setBalance(new BigDecimal("0")); to.setOwner(owner);

        when(cardRepo.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepo.findById(2L)).thenReturn(Optional.of(to));

        assertThatThrownBy(() -> cardService.transfer(1L, 2L, new BigDecimal("100"), "Alice"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Недостаточно средств");
    }

    @Test
    void transferDifferentOwnerThrows() {
        Card from = new Card(); from.setId(1L);
        Card to = new Card(); to.setId(2L);
        User alice = new User(); alice.setUsername("Alice");
        User bob = new User(); bob.setUsername("Kirill");
        from.setBalance(new BigDecimal("1000"));
        from.setOwner(alice);
        to.setBalance(new BigDecimal("0"));
        to.setOwner(bob);

        when(cardRepo.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepo.findById(2L)).thenReturn(Optional.of(to));

        assertThatThrownBy(() -> cardService.transfer(1L, 2L, new BigDecimal("100"), "alice"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Вы можете осуществлять переводы только между своими собственными картами");
    }

    @Test
    void getCardDetailsNotOwnerThrows() {
        Card card = new Card(); card.setId(1L);
        User owner = new User(); owner.setUsername("Alice");
        card.setOwner(owner);
        when(cardRepo.findById(1L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.getCardDetails(1L, "Kirill"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Вы можете просматривать только свои собственные карты");
    }

    @Test
    void listUserCardsIncludesRawNumber() {
        Card card = new Card();
        card.setId(5L);
        card.setOwner(new User()); card.getOwner().setId(2L);
        card.setNumber(cardService.encrypt("5555666677778888"));
        card.setMaskedNumber("**** **** **** 8888");
        card.setOwnerName("eve");
        card.setExpiryDate(LocalDate.now().plusYears(1));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(new BigDecimal("200"));

        when(userRepo.findById(2L)).thenReturn(Optional.of(card.getOwner()));
        when(cardRepo.findAllByOwner(any(), any(Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(card)));

        var page = cardService.listUserCards(2L, null, null, Pageable.unpaged());
        CardDto dto = page.getContent().get(0);
        assertThat(dto.getRawNumber()).isEqualTo("5555666677778888");
    }
}