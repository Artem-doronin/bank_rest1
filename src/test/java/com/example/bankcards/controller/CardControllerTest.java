package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardFilter;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardStatusUpdateRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = CardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CardControllerTest {
    private final String BASE_URL = "/api/v1/cards";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateCard() throws Exception {

    }

    @Test
    void createCard_ReturnsCardResponse() throws Exception {
        CardCreateRequest request = createCardRequest();
        CardResponse response = createCardResponse();

        when(cardService.createCard(any(CardCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()));
    }

    @Test
    void getCard_ReturnsCard() throws Exception {
        Long cardId = 1L;
        CardResponse response = createCardResponse();

        when(cardService.getCardById(eq(cardId), eq(true))).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{cardId}", cardId)
                        .param("showFullNumber", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()));
    }

    @Test
    void getUserCards_ReturnsList() throws Exception {
        Long userId = 1L;
        List<CardResponse> cards = List.of(createCardResponse(), createCardResponse());

        when(cardService.getCardsByUserId(userId)).thenReturn(cards);

        mockMvc.perform(get(BASE_URL + "/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(cards.size())));
    }


    @Test
    void updateCardStatus_NoContent() throws Exception {
        Long cardId = 1L;
        CardStatusUpdateRequest request = createStatusUpdateRequest();

        doNothing().when(cardService).updateCardStatus(eq(cardId), any());

        mockMvc.perform(patch(BASE_URL + "/{cardId}/status", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }


    @Test
    void transfer_AsUser_ReturnsTransferResponse() throws Exception {
        TransferRequest request = createTransferRequest();
        TransferResponse response = createTransferResponse();

        when(cardService.transferFunds(any())).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(response.getTransactionId()));
    }

    @Test
    void deleteCard_NoContent() throws Exception {
        Long cardId = 1L;

        doNothing().when(cardService).deleteCard(cardId);

        mockMvc.perform(delete(BASE_URL + "/{cardId}", cardId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllCards_ReturnsList() throws Exception {
        List<CardResponse> cards = List.of(createCardResponse(),createCardResponse());

        when(cardService.getAllCards()).thenReturn(cards);

        mockMvc.perform(get(BASE_URL+"/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(cards.size())));
    }



    @Test
    void getUserCardsPage_ReturnsPage() throws Exception {
        CardFilter filter = new CardFilter("4242");
        CardResponse cardResponse = createCardResponse();
        Page<CardResponse> page = new PageImpl<>(List.of(cardResponse));

        when(cardService.getUserCards(any(CardFilter.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get(BASE_URL+"/user/page")
                        .param("cardNumber", "4242")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    private CardCreateRequest createCardRequest() {
        return CardCreateRequest.builder()
                .userId(1L)
                .expiryDate(LocalDateTime.now())
                .cardNumber("1234123412341234")
                .build();
    }

    private CardResponse createCardResponse() {
        return CardResponse.builder()
                .id(1L)
                .userId(1L)
                .status(CardStatus.ACTIVE)
                .build();
    }

    private CardStatusUpdateRequest createStatusUpdateRequest() {
        return CardStatusUpdateRequest.builder()
                .status(CardStatus.ACTIVE)
                .build();
    }

    private TransferRequest createTransferRequest() {
        return TransferRequest.builder()
                .sourceCardId(1L)
                .destinationCardId(2L)
                .amount(new BigDecimal(10))
                .build();
    }

    private TransferResponse createTransferResponse() {
        return TransferResponse.builder()
                .transactionId("test")
                .build();
    }
}
