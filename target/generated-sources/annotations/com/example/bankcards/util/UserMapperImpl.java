package com.example.bankcards.util;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserStatus;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-23T18:31:30+0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 23 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto userToUserDto(User user) {
        if ( user == null ) {
            return null;
        }

        Long id = null;
        String username = null;
        UserStatus status = null;

        id = user.getId();
        username = user.getUsername();
        status = user.getStatus();

        Set<Long> rolesId = mapRolesToIds(user.getRoles());
        Set<Long> cardsId = mapCardsToIds(user.getCards());

        UserDto userDto = new UserDto( id, username, rolesId, cardsId, status );

        return userDto;
    }
}
