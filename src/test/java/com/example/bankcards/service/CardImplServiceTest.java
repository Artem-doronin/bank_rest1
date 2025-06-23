package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardStatusUpdateRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardAlreadyExistsException;
import com.example.bankcards.exception.CardBlockedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidCardIdException;
import com.example.bankcards.exception.InvalidUserIdException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.SecurityAccessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class CardImplServiceTest {
    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityAccessService securityAccessService;
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
    public void testPositiveGetCardById() {
        CardResponse cardResponse = createCardResponseMask();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(createCard()));
        doNothing().when(securityAccessService).checkUserAccess(1L);

        CardResponse response1 = cardService.getCardById(1L, true);

        assertEquals(cardResponse.getId(), response1.getId());
    }

    @Test
    public void testPositiveGetCardByUserId() {
        User user = createUser();
        Card card = createCard();
        doNothing().when(securityAccessService).checkUserAccess(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findByOwner(user)).thenReturn(List.of(card));

        List<CardResponse> list = cardService.getCardsByUserId(1L);

        assertEquals(list.size(), 1);
        assertEquals(list.get(0).getId(), card.getId());
    }

    @Test
    public void testPositiveUpdateCardStatus() {
        CardStatusUpdateRequest cardStatusUpdateRequest = createCardStatusUpdateRequest();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(createCard()));
        doNothing().when(securityAccessService).checkUserAccess(1L);

        cardService.updateCardStatus(1L, cardStatusUpdateRequest);

        verify(cardRepository).save(cardCaptor.capture());
        Card capturedCard = cardCaptor.getValue();

        assertEquals(capturedCard.getStatus(), cardStatusUpdateRequest.getStatus());

    }

    @Test
    public void testPositiveTransferFunds() {
        TransferRequest request = createTransferRequest();
        Card sourceCard = createCard();
        sourceCard.setBalance(new BigDecimal("500.00"));
        Card destinationCard = createCard();
        destinationCard.setId(2L);
        destinationCard.setBalance(new BigDecimal("100.00"));

        when(cardRepository.findById(request.getSourceCardId())).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(request.getDestinationCardId())).thenReturn(Optional.of(destinationCard));
        doNothing().when(securityAccessService).checkUserAccess(1L);

        TransferResponse transferResponse = cardService.transferFunds(request);

        verify(cardRepository, times(2)).save(cardCaptor.capture());

        List<Card> capturedCards = cardCaptor.getAllValues();

        assertEquals(0, new BigDecimal(400).compareTo(capturedCards.get(0).getBalance()));
        assertEquals(1L, capturedCards.get(0).getId());

        assertEquals(0, new BigDecimal(200).compareTo(capturedCards.get(1).getBalance()));
        assertEquals(2L, capturedCards.get(1).getId());

        assertNotNull(transferResponse);
    }

    @Test
    public void testNegativeTransferFundsSourceNoActive() {
        TransferRequest request = createTransferRequest();
        Card sourceCard = createCard();
        sourceCard.setBalance(new BigDecimal("500.00"));
        sourceCard.setStatus(CardStatus.BLOCKED);
        Card destinationCard = createCard();
        destinationCard.setId(2L);

        when(cardRepository.findById(request.getSourceCardId())).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(request.getDestinationCardId())).thenReturn(Optional.of(destinationCard));
        doNothing().when(securityAccessService).checkUserAccess(1L);

        assertThrows(CardBlockedException.class, () -> cardService.transferFunds(request));
    }

    @Test
    public void testNegativeTransferFundsInsufficientFunds() {
        TransferRequest request = createTransferRequest();
        Card sourceCard = createCard();
        sourceCard.setStatus(CardStatus.ACTIVE);
        sourceCard.setBalance(new BigDecimal("50.00"));
        Card destinationCard = createCard();
        destinationCard.setId(2L);
        destinationCard.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(request.getSourceCardId())).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(request.getDestinationCardId())).thenReturn(Optional.of(destinationCard));
        doNothing().when(securityAccessService).checkUserAccess(1L);

        assertThrows(InsufficientFundsException.class, () -> cardService.transferFunds(request));
    }

    @Test
    public void testNegativeTransferFundsDestinationNoActive() {
        TransferRequest request = createTransferRequest();
        Card sourceCard = createCard();
        sourceCard.setStatus(CardStatus.ACTIVE);

        Card destinationCard = createCard();
        destinationCard.setId(2L);
        destinationCard.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(request.getSourceCardId())).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(request.getDestinationCardId())).thenReturn(Optional.of(destinationCard));
        doNothing().when(securityAccessService).checkUserAccess(1L);

        assertThrows(CardBlockedException.class, () -> cardService.transferFunds(request));
    }

    @Test
    public void testNegativeUpdateCardStatusCardIdIsNegative() {
        assertThrows(InvalidCardIdException.class,
                () -> cardService.updateCardStatus(-1L, createCardStatusUpdateRequest()));
    }

    @Test
    public void testNegativeUpdateCardStatusCardIdNotFound() {
        assertThrows(InvalidCardIdException.class,
                () -> cardService.updateCardStatus(null, createCardStatusUpdateRequest()));
    }

    @Test
    public void testNegativeGetCardByIdCardNotFound() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardService.getCardById(1L, true));
    }

    @Test
    public void testNegativeGetCardByUserIdUserIdNotFound() {
        assertThrows(InvalidUserIdException.class, () -> cardService.getCardsByUserId(null));
    }

    @Test
    public void testNegativeGetCardByUserIdUserIdNegative() {
        assertThrows(InvalidUserIdException.class, () -> cardService.getCardsByUserId(-1L));
    }

    @Test
    public void testNegativeGetCardByIdUserIdNotFound() {
        assertThrows(InvalidCardIdException.class, () -> cardService.getCardById(null, true));
    }

    @Test
    public void testNegativeGetCardByIdUserIdNegative() {
        assertThrows(InvalidCardIdException.class, () -> cardService.getCardById(-1L, true));
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
                .status(CardStatus.ACTIVE)
                .owner(User.builder()
                        .id(1L)
                        .build())
                .build();
    }

    private CardResponse createCardResponseMask() {
        return CardResponse.builder()
                .maskedCardNumber("1234 1234 1234 1234")
                .balance(new BigDecimal(200))
                .status(CardStatus.ACTIVE)
                .id(1L)
                .userId(1L)
                .build();
    }

    private CardStatusUpdateRequest createCardStatusUpdateRequest() {
        return CardStatusUpdateRequest.builder()
                .status(CardStatus.BLOCKED)
                .build();
    }

    private TransferRequest createTransferRequest() {
        return TransferRequest.builder()
                .sourceCardId(1L)
                .destinationCardId(2L)
                .amount(new BigDecimal(100))
                .description("test")
                .build();
    }
}
