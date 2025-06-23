package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserStatusUpdateRequest;
import com.example.bankcards.entity.UserStatus;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    private final String BASE_URL = "/api/v1/users";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void positiveCreateComment() throws Exception {

        UserStatusUpdateRequest userStatusUpdateRequest = UserStatusUpdateRequest.builder()
                .status(UserStatus.BLOCKED)
                .build();
        Long userId = 1L;

        doNothing().when(userService).updateUserStatus(eq(userId), any(UserStatusUpdateRequest.class));

        mockMvc.perform(patch(BASE_URL + "/{userId}/status", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userStatusUpdateRequest)))
                .andExpect(status().isNoContent());

        verify(userService).updateUserStatus(
                eq(userId),
                argThat(req -> req.getStatus() == UserStatus.BLOCKED));
    }

    @Test
    public void testPositiveGetAllCards() throws Exception {
        List<UserDto> userDtos = List.of(UserDto.builder()
                .id(1L)
                .build());

        when(userService.getAllUsers()).thenReturn(userDtos);

        mockMvc.perform(get(BASE_URL + "/getAll"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userDtos)));
    }

}


