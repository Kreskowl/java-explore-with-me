package ru.practicum.explorewithme.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.dto.UserShortDto;
import ru.practicum.explorewithme.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    User toEntity(NewUserRequest dto);

    UserShortDto toShortDto(User user);
}
