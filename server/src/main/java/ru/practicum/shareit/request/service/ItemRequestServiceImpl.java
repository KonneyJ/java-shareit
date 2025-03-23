package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper requestMapper;

    @Override
    public ItemRequestDto createRequest(int userId, ItemRequestDto itemRequest) {
        User user = getUserById(userId);
        itemRequest.setRequester(user);
        return requestMapper.toItemRequestDto(requestRepository.save(requestMapper.toItemRequest(itemRequest)));
    }

    @Override
    public Collection<ItemRequestDto> getAllRequestsByUser(int userId) {
        User user = getUserById(userId);
        Collection<ItemRequestDto> requests = requestRepository
                .findAllByRequesterIdOrderByCreatedDesc(userId).stream()
                .map(requestMapper::toItemRequestDtoWithResponse)
                .toList();
        return requests;
    }

    @Override
    public Collection<ItemRequestDto> getAllRequests(int userId) {
        User user = getUserById(userId);
        Collection<ItemRequestDto> requests = requestRepository
                .findAllByRequesterIdNotOrderByCreatedDesc(userId).stream()
                .map(requestMapper::toItemRequestDtoWithResponse)
                .toList();
        return requests;
    }

    @Override
    public ItemRequestDto getRequestById(int requestId) {
        ItemRequest itemRequest = requestRepository.findById(requestId).orElseThrow(
                () -> new ItemRequestNotFoundException("Запрос с id = " + requestId + " не найден"));
        /*Collection<Item> itemsResponse = itemRepository.findAllByRequestId(requestId);
        Collection<ItemResponseDto> itemsResponseDto = itemsResponse.stream()
                .map(ItemRequestMapper::toItemResponseDto)
                .toList();*/
        return requestMapper.toItemRequestDtoWithResponse(itemRequest);
    }

    private User getUserById(int userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id = " + userId + " не найден"));
    }
}
