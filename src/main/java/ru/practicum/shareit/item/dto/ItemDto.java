package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private int id;

    @NotNull(message = "Поле name не может быть null")
    @NotBlank(message = "Поле name не может быть пустым")
    private String name;

    @NotNull(message = "Поле description не может быть null")
    @NotBlank(message = "Поле description не может быть пустым")
    private String description;

    @NotNull(message = "Поле available не может быть null")
    @NotBlank(message = "Поле available не может быть пустым")
    private String available;
    private User owner;
    private ItemRequest request;
}
