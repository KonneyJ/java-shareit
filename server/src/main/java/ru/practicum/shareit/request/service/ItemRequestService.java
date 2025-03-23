package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {
    ItemRequestDto createRequest(int userId, ItemRequestDto itemRequestDto);

    Collection<ItemRequestDto> getAllRequestsByUser(int userId);

    Collection<ItemRequestDto> getAllRequests(int userId);

    ItemRequestDto getRequestById(int requestId);
}
