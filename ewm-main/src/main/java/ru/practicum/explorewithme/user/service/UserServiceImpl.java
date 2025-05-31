package ru.practicum.explorewithme.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.exception.custom.NotFoundException;
import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.mapper.UserMapper;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest dto) {
        User user = repository.save(mapper.toEntity(dto));
        return mapper.toDto(user);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, long from, long size) {
        Pageable pageable = PageRequest.of((int) (from / size), (int) size);
        List<User> users = (ids == null || ids.isEmpty())
                ? repository.findAll(pageable).getContent()
                : repository.findAllByIdIn(ids, pageable);
        return users.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("user with id " + id + " not found"));
        repository.delete(user);
    }
}
