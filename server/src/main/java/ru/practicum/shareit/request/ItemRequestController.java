package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader("X-Sharer-User-Id") int userId,
                                        @RequestBody ItemRequestDto itemRequestDto) {
        return requestService.createRequest(userId, itemRequestDto);
    }

    @GetMapping
    public Collection<ItemRequestDto> getAllRequestsByUser(@RequestHeader("X-Sharer-User-Id") int userId) {
        return requestService.getAllRequestsByUser(userId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") int userId) {
        return requestService.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@PathVariable("requestId") int requestId) {
        return requestService.getRequestById(requestId);
    }
}
