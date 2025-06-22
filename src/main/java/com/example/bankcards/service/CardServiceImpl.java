package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardFilter;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardStatusUpdateRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.dto.UserStatusUpdateRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardAlreadyExistsException;
import com.example.bankcards.exception.CardBlockedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UserNotAuthenticatedException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Override
    public CardResponse createCard(CardCreateRequest request) {
        log.info("Попытка создать карту с номером {} для пользователя с ID {}", request.getCardNumber(), request.getUserId());

        if (cardRepository.existsByCardNumber(request.getCardNumber())) {
            log.warn("Карта с номером {} уже существует", request.getCardNumber());
            throw new CardAlreadyExistsException("Карта с таким номером уже существует");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", request.getUserId());
                    return new UserNotFoundException("Пользователь не найден");
                });

        Card card = Card.builder()
                .cardNumber(request.getCardNumber())
                .expiryDate(request.getExpiryDate())
                .owner(user)
                .balance(BigDecimal.ZERO)
                .status(CardStatus.ACTIVE)
                .build();

        cardRepository.save(card);
        log.info("Карта успешно создана для пользователя с ID {}", user.getId());
        return mapToCardResponse(card, false);
    }

    @Override
    public CardResponse getCardById(Long cardId, boolean showFullNumber) {
        log.debug("Запрос карты с ID {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Карта с ID {} не найдена", cardId);
                    return new CardNotFoundException("Карта не найдена с ID: " + cardId);
                });

        checkUserAccess(card.getOwner().getId());

        return mapToCardResponse(card, showFullNumber);
    }

    @Override
    public List<CardResponse> getCardsByUserId(Long userId) {
        log.debug("Получение карт пользователя с ID {}", userId);

        checkUserAccess(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", userId);
                    return new UserNotFoundException("Пользователь не найден");
                });

        List<Card> cards = cardRepository.findByOwner(user);
        log.info("Найдено {} карт для пользователя с ID {}", cards.size(), userId);
        return cards.stream()
                .map(card -> mapToCardResponse(card, false))
                .collect(Collectors.toList());
    }

    @Override
    public void updateCardStatus(Long cardId, CardStatusUpdateRequest request) {
        log.info("Обновление статуса карты с ID {} на {}", cardId, request.getStatus());

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Карта с ID {} не найдена для обновления статуса", cardId);
                    return new CardNotFoundException("Карта не найдена с ID: " + cardId);
                });

        checkUserAccess(card.getOwner().getId());

        card.setStatus(request.getStatus());
        cardRepository.save(card);
        log.info("Статус карты с ID {} успешно обновлен на {}", cardId, request.getStatus());
    }

    @Override
    @Transactional
    public TransferResponse transferFunds(TransferRequest request) {
        log.info("Инициация перевода {} с карты ID {} на карту ID {}",
                request.getAmount(), request.getSourceCardId(), request.getDestinationCardId());

        Card sourceCard = cardRepository.findById(request.getSourceCardId())
                .orElseThrow(() -> {
                    log.error("Исходящая карта с ID {} не найдена", request.getSourceCardId());
                    return new CardNotFoundException("Исходящая карта не найдена");
                });

        Card destinationCard = cardRepository.findById(request.getDestinationCardId())
                .orElseThrow(() -> {
                    log.error("Целевая карта с ID {} не найдена", request.getDestinationCardId());
                    return new CardNotFoundException("Целевая карта не найдена");
                });

        checkUserAccess(sourceCard.getOwner().getId());

        if (sourceCard.getStatus() != CardStatus.ACTIVE) {
            log.warn("Исходящая карта с ID {} заблокирована или неактивна", sourceCard.getId());
            throw new CardBlockedException("Исходящая карта заблокирована или неактивна");
        }
        if (destinationCard.getStatus() != CardStatus.ACTIVE) {
            log.warn("Целевая карта с ID {} заблокирована или неактивна", destinationCard.getId());
            throw new CardBlockedException("Целевая карта заблокирована или неактивна");
        }

        if (sourceCard.getBalance().compareTo(request.getAmount()) < 0) {
            log.warn("Недостаточно средств на карте ID {} для перевода {}", sourceCard.getId(), request.getAmount());
            throw new InsufficientFundsException("Недостаточно средств для перевода");
        }

        sourceCard.setBalance(sourceCard.getBalance().subtract(request.getAmount()));
        destinationCard.setBalance(destinationCard.getBalance().add(request.getAmount()));

        cardRepository.save(sourceCard);
        cardRepository.save(destinationCard);

        String transactionId = java.util.UUID.randomUUID().toString();
        log.info("Перевод выполнен успешно. ID транзакции: {}", transactionId);

        return TransferResponse.builder()
                .transactionId(transactionId)
                .newSourceBalance(sourceCard.getBalance())
                .newDestinationBalance(destinationCard.getBalance())
                .status("SUCCESS")
                .build();
    }

    @Override
    public void deleteCard(Long cardId) {
        log.info("Запрос на удаление карты с ID {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Карта с ID {} не найдена для удаления", cardId);
                    return new CardNotFoundException("Карта не найдена с ID: " + cardId);
                });

        checkUserAccess(card.getOwner().getId());

        cardRepository.delete(card);
        log.info("Карта с ID {} успешно удалена", cardId);
    }

    @Override
    public List<CardResponse> getAllCards() {
        log.debug("Получение всех карт");

        List<Card> cards = cardRepository.findAll();
        log.info("Найдено {} карт", cards.size());

        return cards.stream()
                .map(card -> mapToCardResponse(card, false))
                .collect(Collectors.toList());
    }

    @Override
    public Page<CardResponse> getUserCards(CardFilter filter, Pageable pageable) {
        Long currentUserId = getCurrentUserId();
        log.debug("Получение страниц карт пользователя с ID {} с фильтром по номеру: {}", currentUserId, filter.cardNumber());

        Page<Card> cardPage = cardRepository.findByUserIdAndCardNumber(currentUserId,
                filter.cardNumber(), pageable);

        return cardPage.map(this::mapToCardResponseNoFullNumber);
    }

    private void checkUserAccess(Long cardUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.error("Пользователь не аутентифицирован");
            throw new UserNotAuthenticatedException("Пользователь не аутентифицирован");
        }

        String currentUserRole = auth.getAuthorities().toString();
        Long currentUserId = getCurrentUserId();

        if (currentUserRole.contains("ROLE_ADMIN")) {
            log.debug("Пользователь с ID {} имеет роль ADMIN, доступ разрешён", currentUserId);
            return;
        }

        if (!currentUserId.equals(cardUserId)) {
            log.warn("Пользователь с ID {} пытается получить доступ к данным пользователя с ID {}", currentUserId, cardUserId);
            throw new AccessDeniedException("Нет прав для доступа к этой карте");
        }
        log.debug("Пользователь с ID {} имеет доступ", currentUserId);
    }

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.error("Пользователь не аутентифицирован");
            throw new UserNotAuthenticatedException("Пользователь не аутентифицирован");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            Long id = ((CustomUserDetails) principal).getId();
            log.debug("Текущий пользователь аутентифицирован с ID {}", id);
            return id;
        } else {
            log.error("Принципал не является экземпляром CustomUserDetails");
            throw new UserNotAuthenticatedException("Не удалось определить пользователя");
        }
    }

    private CardResponse mapToCardResponse(Card card, boolean showFullNumber) {
        String displayNumber = showFullNumber ? card.getCardNumber() : maskCardNumber(card.getCardNumber());

        return CardResponse.builder()
                .id(card.getId())
                .maskedCardNumber(displayNumber)
                .expiryDate(card.getExpiryDate())
                .balance(card.getBalance())
                .userId(card.getOwner().getId())
                .build();
    }

    private CardResponse mapToCardResponseNoFullNumber(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .maskedCardNumber(maskCardNumber(card.getCardNumber()))
                .expiryDate(card.getExpiryDate())
                .balance(card.getBalance())
                .userId(card.getOwner().getId())
                .build();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}


