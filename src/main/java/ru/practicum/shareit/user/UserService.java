package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    Collection<UserDto> getAllUsers();

    UserDto getUserById(int id);

    UserDto createUser(UserDto user);

    UserDto updateUser(int id, UserDto user);

    void deleteUser(int id);
}
