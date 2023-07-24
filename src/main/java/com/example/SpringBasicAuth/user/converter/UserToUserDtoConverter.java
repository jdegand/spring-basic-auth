package com.example.SpringBasicAuth.user.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.example.SpringBasicAuth.user.User;
import com.example.SpringBasicAuth.user.dto.UserDto;

@Component
public class UserToUserDtoConverter implements Converter<User, UserDto> {

    @Override
    public UserDto convert(User source) {
        final UserDto userDto = new UserDto(source.getId(),
                source.getUsername(),
                source.getRoles(),
                source.isEnabled());
        return userDto;
    }

}
