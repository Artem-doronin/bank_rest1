package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardFilter;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardStatusUpdateRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Card Management", description = "API для управления банковскими картами")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class CardController {
    private final CardService cardService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новую карту", description = "Доступно только администраторам")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardCreateRequest request) {
        return ResponseEntity.ok(cardService.createCard(request));
    }

    @GetMapping("/{cardId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Получить данные карты по ID")
    public ResponseEntity<CardResponse> getCard(
            @PathVariable Long cardId,
            @RequestParam(required = false) boolean showFullNumber // Опционально: показывать полный номер
    ) {
        return ResponseEntity.ok(cardService.getCardById(cardId, showFullNumber));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Получить все карты пользователя")
    public ResponseEntity<List<CardResponse>> getUserCards(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.getCardsByUserId(userId));
    }

    @PatchMapping("/{cardId}/status")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Изменить статус карты (активна/заблокирована)")
    public ResponseEntity<Void> updateCardStatus(
            @PathVariable Long cardId,
            @Valid @RequestBody CardStatusUpdateRequest request
    ) {
        cardService.updateCardStatus(cardId, request);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Перевести средства между картами")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request
    ) {
        return ResponseEntity.ok(cardService.transferFunds(request));
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить карту", description = "Доступно только администраторам")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole( 'ADMIN')")
    @Operation(summary = "Получить все карты ", description = "Доступно только администраторам")
    public ResponseEntity<List<CardResponse>> getAllCards() {
        return ResponseEntity.ok(cardService.getAllCards());
    }

    @GetMapping("/user/page")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Получить карты пользователя с фильтрацией",
            description = """
                    ### Доступ:
                    - Только владелец карты или Admin.
                    
                    ### Фильтры:
                    - `cardNumber`: Поиск по частичному совпадению номера карты (без учёта регистра).
                    - `page`: Номер страницы (по умолчанию 0).
                    - `size`: Размер страницы (по умолчанию 10).
                    
                    ### Пример:
                    `GET /user/page/1?cardNumber=4242&page=0&size=5`
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный запрос"),
                    @ApiResponse(responseCode = "204", description = "Нет данных"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещён")
            }
    )
    public ResponseEntity<Page<CardResponse>> getUserCards(
            @RequestParam(required = false)
            @Parameter(description = "Фильтр по номеру карты (подстрока)") String cardNumber,
            @Parameter(hidden = true) @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        CardFilter filter = new CardFilter(
                cardNumber != null ? cardNumber.trim() : null
        );

        Page<CardResponse> cards = cardService.getUserCards(filter, pageable);

        return cards.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(cards);
    }
}

