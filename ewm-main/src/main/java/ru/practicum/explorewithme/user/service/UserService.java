package ru.practicum.explorewithme.user.service;

import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest dto);

    List<UserDto> getUsers(List<Long> ids, long from, long size);

    void deleteUser(long id);
}
