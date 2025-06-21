package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardFilter;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardStatusUpdateRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.dto.UserStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CardService {
    CardResponse createCard(CardCreateRequest request);

    CardResponse getCardById(Long cardId, boolean showFullNumber);

    List<CardResponse> getCardsByUserId(Long userId);

    void updateCardStatus(Long cardId, CardStatusUpdateRequest request);

    TransferResponse transferFunds(TransferRequest request);

    void deleteCard(Long cardId);

    List<CardResponse> getAllCards();

    Page<CardResponse> getUserCards(CardFilter filter, Pageable pageable);

    void updateUserStatus(Long userId, UserStatusUpdateRequest request);
}

