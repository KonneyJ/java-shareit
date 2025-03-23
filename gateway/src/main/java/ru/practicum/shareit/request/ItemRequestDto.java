package ru.practicum.shareit.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private int id;

    @NotNull(message = "Поле description не может быть null")
    @NotBlank(message = "Поле description не может быть пустым")
    private String description;

    private UserDto requester;

    private LocalDateTime created;

    private Collection<ItemDto> items;
}
