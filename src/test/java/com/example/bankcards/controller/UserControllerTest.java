package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.UserStatusUpdateRequest;
import com.example.bankcards.entity.UserStatus;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;


    @Test
    @WithMockUser(username = "alex", roles = "ADMIN")
    void positiveCreateComment() throws Exception {

        UserStatusUpdateRequest userStatusUpdateRequest = UserStatusUpdateRequest.builder()
                .status(UserStatus.BLOCKED)
                .build();
        Long userId = 1L;

        doNothing().when(userService).updateUserStatus(eq(userId), any(UserStatusUpdateRequest.class));


        mockMvc.perform(patch("/{userId}/status", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userStatusUpdateRequest)))
                .andExpect(status().isNoContent());

        verify(userService).updateUserStatus(
                eq(userId),
                argThat(req -> req.getStatus() == UserStatus.BLOCKED));
    }
}


