package com.example.bankcards.controller;


import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardStatusUpdateRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Card Management", description = "API для управления банковскими картами")
@SecurityRequirement(name = "Bearer Authentication")
public class CardController {
    private final CardService cardService;

    // === Создание карты (только ADMIN) ===
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новую карту", description = "Доступно только администраторам")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardCreateRequest request) {
        return ResponseEntity.ok(cardService.createCard(request));
    }

    // === Получение информации о карте ===
    @GetMapping("/{cardId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Получить данные карты по ID")
    public ResponseEntity<CardResponse> getCard(
            @PathVariable Long cardId,
            @RequestParam(required = false) boolean showFullNumber // Опционально: показывать полный номер
    ) {
        return ResponseEntity.ok(cardService.getCardById(cardId, showFullNumber));
    }

    // === Получение списка карт пользователя ===
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Получить все карты пользователя")
    public ResponseEntity<List<CardResponse>> getUserCards(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.getCardsByUserId(userId));
    }

    // === Блокировка/разблокировка карты ===
    @PatchMapping("/{cardId}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Изменить статус карты (активна/заблокирована)")
    public ResponseEntity<Void> updateCardStatus(
            @PathVariable Long cardId,
            @Valid @RequestBody CardStatusUpdateRequest request
    ) {
        cardService.updateCardStatus(cardId, request);
        return ResponseEntity.noContent().build();
    }

    // === Перевод между картами ===
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Перевести средства между картами")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request
    ) {
        return ResponseEntity.ok(cardService.transferFunds(request));
    }

    // === Удаление карты (ADMIN) ===
    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить карту", description = "Доступно только администраторам")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}

