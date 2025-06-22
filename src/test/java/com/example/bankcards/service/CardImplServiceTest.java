package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardAlreadyExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class CardImplServiceTest {
    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Captor
    private ArgumentCaptor<Card> cardCaptor;
    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    public void testPositiveCreateCard() {
        CardCreateRequest request = createCardRequest();
        User user = createUser();


        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.save(any())).thenReturn(createCard());

        cardService.createCard(request);

        verify(cardRepository).save(cardCaptor.capture());
        Card capturedCard = cardCaptor.getValue();

        assertEquals(request.getCardNumber(), capturedCard.getCardNumber());
        assertEquals(request.getExpiryDate(), capturedCard.getExpiryDate());
        assertEquals(user, capturedCard.getOwner());
    }

    @Test
    public void testNegativeCreateCardExistsByCardNumber() {
        CardCreateRequest request = createCardRequest();

        when(cardRepository.existsByCardNumber(anyString()))
                .thenReturn(true);
        assertThrows(CardAlreadyExistsException.class,
                () -> cardService.createCard(request));
        verify(cardRepository, never()).save(any());
    }

    @Test
    public void testNegativeCreateUserNotFound() {
        CardCreateRequest request = createCardRequest();

        when(cardRepository.existsByCardNumber(anyString()))
                .thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> cardService.createCard(request));
        verify(cardRepository, never()).save(any());
    }

    private CardCreateRequest createCardRequest() {
        return CardCreateRequest.builder()
                .cardNumber("1234123412341234")
                .expiryDate(LocalDateTime.now().plusMonths(1))
                .userId(1L)
                .build();
    }

    private User createUser() {
        return User.builder()
                .id(1L)
                .build();
    }

    private Card createCard() {
        return Card.builder()
                .id(1L)
                .build();
    }
}
