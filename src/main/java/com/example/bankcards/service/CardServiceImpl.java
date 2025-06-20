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
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Override
    public CardResponse createCard(CardCreateRequest request) {
        // Проверка, что карта с таким номером не существует
        if (cardRepository.existsByCardNumber(request.getCardNumber())) {
            throw new CardAlreadyExistsException("Card with this number already exists");
        }
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(()->new UserNotFoundException("User not found"));

        Card card = Card.builder()
                .cardNumber(request.getCardNumber())
                .expiryDate(request.getExpiryDate())
                .owner(user)
                .balance(BigDecimal.valueOf(0))
                .status(CardStatus.ACTIVE)
                .build();

        Card savedCard = cardRepository.save(card);
        return mapToCardResponse(savedCard, false); // Не показываем полный номер при создании
    }

    @Override
    public CardResponse getCardById(Long cardId, boolean showFullNumber) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + cardId));

        // Проверка прав доступа (только владелец или админ)
        checkUserAccess(card.getOwner().getId());

        return mapToCardResponse(card, showFullNumber);
    }

    @Override
    public List<CardResponse> getCardsByUserId(Long userId) {
        // Проверка прав доступа (только владелец или админ)
        checkUserAccess(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException("User not found"));

        List<Card> cards = cardRepository.findByOwner(user);
        return cards.stream()
                .map(card -> mapToCardResponse(card, false)) // Маскируем номер для списка
                .collect(Collectors.toList());
    }

    @Override
    public void updateCardStatus(Long cardId, CardStatusUpdateRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + cardId));

        checkUserAccess(card.getOwner().getId());

        card.setStatus(request.getStatus());
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public TransferResponse transferFunds(TransferRequest request) {
        Card sourceCard = cardRepository.findById(request.getSourceCardId())
                .orElseThrow(() -> new CardNotFoundException("Source card not found"));

        Card destinationCard = cardRepository.findById(request.getDestinationCardId())
                .orElseThrow(() -> new CardNotFoundException("Destination card not found"));

        // Проверка, что отправитель — владелец исходной карты
        checkUserAccess(sourceCard.getOwner().getId());

        // Проверка, что обе карты активны (ACTIVE)
        if (sourceCard.getStatus() != CardStatus.ACTIVE || destinationCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardBlockedException("Одна из карт заблокирована или неактивна");
        }

        // Проверка достаточности средств (используем compareTo())
        if (sourceCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        // Выполнение перевода (используем subtract() и add())
        sourceCard.setBalance(sourceCard.getBalance().subtract(request.getAmount()));
        destinationCard.setBalance(destinationCard.getBalance().add(request.getAmount()));

        cardRepository.save(sourceCard);
        cardRepository.save(destinationCard);

        return TransferResponse.builder()
                .transactionId(java.util.UUID.randomUUID().toString())
                .newSourceBalance(sourceCard.getBalance())
                .newDestinationBalance(destinationCard.getBalance())
                .status("SUCCESS")
                .build();
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + cardId));

                // Проверка прав доступа (только владелец или админ)
                checkUserAccess(card.getOwner().getId());

        // Мягкое удаление (установка флага isDeleted) или полное удаление
        cardRepository.delete(card); // или card.setDeleted(true); cardRepository.save(card);
    }

// ===== Вспомогательные методы =====

    /**
     * Проверяет, имеет ли текущий пользователь доступ к карте.
     * @param cardUserId ID пользователя, которому принадлежит карта.
     * @throws AccessDeniedException если доступ запрещен.
     */
    private void checkUserAccess(Long cardUserId) {
        String currentUserRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
        Long currentUserId = getCurrentUserId();

        // Админ может управлять любыми картами
        if (currentUserRole.contains("ROLE_ADMIN")) {
            return;
        }

        // Обычный пользователь может управлять только своими картами
        if (!currentUserId.equals(cardUserId)) {
            throw new AccessDeniedException("You don't have permission to access this card");
        }
    }

    /**
     * Получает ID текущего аутентифицированного пользователя.
     */
    private Long getCurrentUserId() {
        // Пример: если userId хранится в JWT как строка
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return Long.parseLong(userId);
    }

    /**
     * Преобразует сущность Card в CardResponse.
     * @param showFullNumber Если false, маскирует номер карты (**** **** **** 1234).
     */
    private CardResponse mapToCardResponse(Card card, boolean showFullNumber) {
        String maskedCardNumber = maskCardNumber(card.getCardNumber());
        String displayNumber = showFullNumber ? card.getCardNumber() : maskedCardNumber;

        return CardResponse.builder()
                .id(card.getId())
                .maskedCardNumber(displayNumber)
                .expiryDate(card.getExpiryDate())
                .balance(card.getBalance())
                .userId(card.getOwner().getId())
                .build();
    }

    /**
     * Маскирует номер карты, оставляя видимыми только последние 4 цифры.
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
