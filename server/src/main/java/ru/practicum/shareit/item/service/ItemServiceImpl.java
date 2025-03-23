package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ConditionException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository requestRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    public ItemDto createItem(int userId, ItemSaveDto itemDto) {
        log.info("Creating item {}, userId={}", itemDto, userId);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id = " + userId + " не найден"));
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(user);
        Integer requestId = itemDto.getRequestId();
        if (requestId != null) {
            ItemRequest request = requestRepository.findById(requestId).orElseThrow(
                    () -> new ItemRequestNotFoundException("Запрос с id = " + requestId + " не найден"));
            item.setRequest(request);
        }
        log.info("Item has been created {}, userId={}", item, userId);
        return itemMapper.toItemDto(itemRepository.save(item));

    }

    @Override
    public ItemDto updateItem(int userId, int id, ItemDto itemDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id = " + userId + " не найден"));
        Item item = itemRepository.findById(id).orElseThrow(
                () -> new ItemNotFoundException("Вещь с id = " + id + " не найдена"));
        item.setId(id);
        if (user.getId() != item.getOwner().getId()) {
            throw new UserNotFoundException("Пользователь может обновлять только свои вещи");
        }
        item.setOwner(user);
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getItemById(int id) {
        Item item = itemRepository.findById(id).orElseThrow(
                () -> new ItemNotFoundException("Вещь с id = " + id + " не найдена"));

        Collection<Booking> nextBookings = bookingRepository.findAllByItemIdAndStartAfterOrderByStartAsc(id,
                LocalDateTime.now());
        LocalDateTime nextBooking = nextBookings.stream()
                .map(Booking::getStart)
                .findFirst()
                .orElse(null);
        LocalDateTime lastBooking = nextBookings.stream()
                .map(Booking::getStart)
                .reduce((first, last) -> last)
                .orElse(null);

        Collection<Comment> comments = commentRepository.findAllByItemId(id);
        Collection<CommentDto> commentsDto = comments.stream().map(CommentMapper::toCommentDto).toList();

        ItemDto itemDto = itemMapper.toItemDto(item, lastBooking, nextBooking, commentsDto);
        return itemDto;
    }

    @Override
    public Collection<ItemDto> getAllItems(int userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id = " + userId + " не найден"));

        Collection<ItemDto> itemsDto = itemRepository.findAllByOwnerId(userId).stream().map(ItemMapper::toItemDto).toList();
        Collection<Integer> itemsId = itemsDto.stream().map(ItemDto::getId).toList();

        Collection<Booking> futureBookings = bookingRepository
                .findAllByItemIdInAndStartAfterOrderByStartDesc(itemsId, LocalDateTime.now());

        Collection<Booking> pastBookings = bookingRepository
                .findAllByItemIdInAndEndBeforeOrderByStartDesc(itemsId, LocalDateTime.now());

        Collection<Comment> comments = commentRepository.findAllByItemIdIn(itemsId);
        Collection<CommentDto> commentsDto = comments.stream().map(CommentMapper::toCommentDto).toList();

        for (ItemDto itemDto : itemsDto) {
            for (Booking futureBooking : futureBookings) {
                if (futureBooking.getItem().getId() == itemDto.getId()) {
                    if (itemDto.getNextBooking().isAfter(futureBooking.getStart())) {
                        itemDto.setNextBooking(futureBooking.getStart());
                    }
                }
            }

            for (Booking pastBooking : pastBookings) {
                if (pastBooking.getItem().getId() == itemDto.getId()) {
                    if (itemDto.getLastBooking().isBefore(pastBooking.getStart())) {
                        itemDto.setLastBooking(pastBooking.getStart());
                    }
                }
            }

            Collection<CommentDto> commentsByItem = itemDto.getComments();
            for (CommentDto commentDto : commentsDto) {
                if (commentDto.getItem().getId() == itemDto.getId()) {
                    if (commentsByItem == null) {
                        commentsByItem = new ArrayList<>();
                    }
                    commentsByItem.add(commentDto);
                }
            }
            itemDto.setComments(commentsByItem);
        }
        return itemsDto;
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        if (text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        } else {
            return itemRepository.searchItems(text).stream().map(ItemMapper::toItemDto).toList();
        }
    }

    @Override
    public CommentDto addComment(int userId, int itemId, CommentDto comment) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id = " + userId + " не найден"));
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException("Вещь с id = " + itemId + " не найдена"));
        Booking booking = bookingRepository
                .findByBookerIdAndItemIdAndEndBeforeOrderByStartDesc(userId, itemId, LocalDateTime.now());
        if (booking == null) {
            throw new ConditionException("Бронирование вещи не подтверждено");
        }
        comment.setItem(item);
        comment.setAuthorName(user.getName());
        return commentMapper.toCommentDto(commentRepository.save(commentMapper.toComment(comment, user)));
    }

    @Override
    public Collection<ItemResponseDto> getItemsByRequestId(int requestId) {
        Collection<ItemResponseDto> items = itemRepository.findAllByRequestId(requestId).stream()
                .map(ItemMapper::toItemResponseDto)
                .toList();
        return items;
    }
}
