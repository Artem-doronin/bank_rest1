package com.example.bankcards.service;

import com.example.bankcards.dto.UserStatusUpdateRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserStatus;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.UserMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserImplServiceTest {

    @Mock
    private UserRepository userRepository;
    @Spy
    private UserMapperImpl userMapper;
    @InjectMocks
    private UserServiceImpl userService;
    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    public void testPositiveUpdateUserStatus() {
        UserStatusUpdateRequest userStatusUpdateRequest = UserStatusUpdateRequest.builder()
                .status(UserStatus.BLOCKED)
                .build();
        User user = createUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.updateUserStatus(user.getId(), userStatusUpdateRequest);

        verify(userRepository,times(1)).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();

        assertEquals(userStatusUpdateRequest.getStatus(), updatedUser.getStatus());
    }

    private User createUser() {
        return User.builder()
                .id(1L)
                .status(UserStatus.ACTIVE)
                .build();
    }

}
