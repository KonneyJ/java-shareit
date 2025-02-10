package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Override
    public Collection<UserDto> getAllUsers() {
        return userStorage.getAllUsers().stream().map(UserMapper::toUserDto).toList();
    }

    @Override
    public UserDto getUserById(int id) {
        return userMapper.toUserDto(userStorage.getUserById(id));
    }

    @Override
    public UserDto createUser(UserDto user) {
        return userMapper.toUserDto(userStorage.createUser(userMapper.toUser(user)));
    }

    @Override
    public UserDto updateUser(int id, UserDto user) {
        return userMapper.toUserDto(userStorage.updateUser(id, userMapper.toUser(user)));
    }

    @Override
    public void deleteUser(int id) {
        userStorage.deleteUser(id);
    }
}
