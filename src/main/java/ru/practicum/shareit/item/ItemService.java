package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {

    ItemDto createItem(int userId, ItemDto item);

    ItemDto updateItem(int userId, int id, ItemDto item);

    ItemDto getItemById(int id);

    Collection<ItemDto> getAllItems(int userId);

    Collection<ItemDto> searchItems(String text);
}
