package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;

import java.util.Collection;

public interface ItemService {

    ItemDto createItem(int userId, ItemSaveDto item);

    ItemDto updateItem(int userId, int id, ItemDto item);

    ItemDto getItemById(int id);

    Collection<ItemDto> getAllItems(int userId);

    Collection<ItemDto> searchItems(String text);

    CommentDto addComment(int userId, int itemId, CommentDto comment);

    Collection<ItemResponseDto> getItemsByRequestId(int requestId);
}
