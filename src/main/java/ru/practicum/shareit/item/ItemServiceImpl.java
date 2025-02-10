package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final ItemMapper itemMapper;
    private final UserStorage userStorage;

    @Override
    public ItemDto createItem(int userId, ItemDto item) {
        User user = userStorage.getUserById(userId);
        return itemMapper.toItemDto(itemStorage.createItem(user, item));
    }

    @Override
    public ItemDto updateItem(int userId, int id, ItemDto item) {
        User user = userStorage.getUserById(userId);
        return itemMapper.toItemDto(itemStorage.updateItem(user, id, item));
    }

    @Override
    public ItemDto getItemById(int id) {
        return itemMapper.toItemDto(itemStorage.getItemById(id));
    }

    @Override
    public Collection<ItemDto> getAllItems(int userId) {
        return itemStorage.getAllItems(userId).stream().map(ItemMapper::toItemDto).toList();
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        return itemStorage.searchItems(text).stream().map(ItemMapper::toItemDto).toList();
    }
}
