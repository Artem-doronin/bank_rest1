package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardStatusUpdateRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CardServiceImpl implements CardService {

    @Override
    @Transactional
    public CardResponse createCard(CardCreateRequest request) {
        // Проверка, что карта с таким номером не существует
        if (cardRepository.existsByCardNumber(request.getCardNumber())) {
            throw new CardAlreadyExistsException("Card with this number already exists");
        }

        Card card = Card.builder()
                .cardholderName(request.getCardholderName())
                .cardNumber(request.getCardNumber())
                .cvv(request.getCvv())
                .expiryDate(request.getExpiryDate())
                .userId(request.getUserId())
                .cardType(request.getCardType())
                .balance(request.getBalance() != null ? request.getBalance() : 0.0)
                .isActive(true)
                .build();

        Card savedCard = cardRepository.save(card);
        return mapToCardResponse(savedCard, false); // Не показываем полный номер при создании
    }

    @Override
    public CardResponse getCardById(Long cardId, boolean showFullNumber) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + cardId));

        // Проверка прав доступа (только владелец или админ)
        checkUserAccess(card.getUserId());

        return mapToCardResponse(card, showFullNumber);
    }

    @Override
    public List<CardResponse> getCardsByUserId(Long userId) {
        // Проверка прав доступа (только владелец или админ)
        checkUserAccess(userId);

        List<Card> cards = cardRepository.findByUserId(userId);
        return cards.stream()
                .map(card -> mapToCardResponse(card, false)) // Маскируем номер для списка
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateCardStatus(Long cardId, CardStatusUpdateRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + cardId));

        checkUserAccess(card.getUserId());

        card.setIsActive(request.getIsActive());
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
        checkUserAccess(sourceCard.getUserId());

        // Проверка, что карта активна
        if (!sourceCard.getIsActive() || !destinationCard.getIsActive()) {
            throw new CardBlockedException("One of the cards is blocked");
        }

        // Проверка достаточности средств
        if (sourceCard.getBalance() < request.getAmount()) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        // Выполнение перевода
        sourceCard.setBalance(sourceCard.getBalance() - request.getAmount());
        destinationCard.setBalance(destinationCard.getBalance() + request.getAmount());

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
        Card card = cardRepository.findById(cardId.orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + cardId));

                // Проверка прав доступа (только владелец или админ)
                checkUserAccess(card.getUserId());

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
                .cardholderName(card.getCardholderName())
                .cardNumber(displayNumber)
                .expiryDate(card.getExpiryDate())
                .cardType(card.getCardType())
                .balance(card.getBalance())
                .isActive(card.getIsActive())
                .userId(card.getUserId())
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
