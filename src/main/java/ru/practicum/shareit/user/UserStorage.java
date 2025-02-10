package ru.practicum.shareit.user;

import java.util.Collection;

public interface UserStorage {
    Collection<User> getAllUsers();

    User getUserById(int id);

    User createUser(User user);

    User updateUser(int id, User user);

    void deleteUser(int id);
}
