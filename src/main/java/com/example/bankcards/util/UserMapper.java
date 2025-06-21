package com.example.bankcards.util;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "rolesId", expression = "java(mapRolesToIds(user.getRoles()))")
    @Mapping(target = "cardsId", expression = "java(mapCardsToIds(user.getCards()))")
    UserDto userToUserDto(User user);


    default Set<Long> mapRolesToIds(Set<Role> roles) {
        return Optional.ofNullable(roles)
                .map(r -> r.stream().map(Role::getId).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    default Set<Long> mapCardsToIds(Set<Card> cards) {
        return Optional.ofNullable(cards)
                .map(r -> r.stream().map(Card::getId).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

}
