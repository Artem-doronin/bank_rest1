package com.example.bankcards.controller;

import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@ContextConfiguration(classes = CardController.class)
public class CardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;
}
