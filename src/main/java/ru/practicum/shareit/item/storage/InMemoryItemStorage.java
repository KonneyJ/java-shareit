package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ConditionException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class InMemoryItemStorage implements ItemStorage {
    private int nextId = 0;
    private final Map<Integer, Item> items = new HashMap<>();
    private final ItemMapper itemMapper;

    @Override
    public Item createItem(User user, ItemDto item) {
        if (item.getName() == null || item.getName().isEmpty() || item.getDescription() == null) {
            throw new ConditionException("Имя и описание должны быть заполнены");
        }
        item.setId(getNextId());
        item.setOwner(user);
        if (item.getRequest() == null) {
            item.setRequest(new ItemRequest());
        }
        if (item.getAvailable() == null) {
            throw new ConditionException("Статус доступности вещи должен быть заполнен");
        }
        Item itemToStorage = itemMapper.toItem(item);
        items.put(item.getId(), itemToStorage);
        return itemToStorage;
    }

    @Override
    public Item updateItem(User user, int id, ItemDto item) {
        if (user.getId() != items.get(id).getOwner().getId()) {
            throw new UserNotFoundException("Пользователь может обновлять только свои вещи");
        }
        if (!items.containsKey(id)) {
            throw new ItemNotFoundException("Вещь с id = " + id + " не найдена. Обновление невозможно");
        }
        Item itemFromStorage = items.get(id);
        itemFromStorage.setId(id);
        if (item.getName() != null) {
            itemFromStorage.setName(item.getName());
        }
        if (item.getDescription() != null) {
            itemFromStorage.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            if (item.getAvailable().equals("true")) {
                itemFromStorage.setAvailable(true);
            }
            if (item.getAvailable().equals("false")) {
                itemFromStorage.setAvailable(false);
            }
        }
        items.put(itemFromStorage.getId(), itemFromStorage);
        return itemFromStorage;
    }

    @Override
    public Item getItemById(int id) {
        if (items.containsKey(id)) {
            return items.get(id);
        } else {
            throw new ItemNotFoundException("Вещь с id = " + id + " не найдена");
        }
    }

    @Override
    public Collection<Item> getAllItems(int userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId() == userId)
                .toList();
    }

    @Override
    public Collection<Item> searchItems(String text) {
        if (text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        } else {
            return items.values().stream()
                    .filter(Item::getAvailable)
                    .filter(item -> item.getName().equalsIgnoreCase(text) ||
                            item.getDescription().equalsIgnoreCase(text))
                    .toList();
        }
    }

    private int getNextId() {
        return nextId++;
    }
}
