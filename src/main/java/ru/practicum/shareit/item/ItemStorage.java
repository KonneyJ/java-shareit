package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.Collection;

public interface ItemStorage {

    Item createItem(User user, ItemDto item);

    Item updateItem(User user, int id, ItemDto item);

    Item getItemById(int id);

    Collection<Item> getAllItems(int userId);

    Collection<Item> searchItems(String text);
}
