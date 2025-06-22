package com.example.bankcards.dto;

import com.example.bankcards.entity.UserStatus;
import lombok.Builder;
import java.util.Set;
@Builder
public record UserDto (
        Long id,
        String username,
        Set<Long> rolesId,
        Set<Long> cardsId,
        UserStatus status
){
}
