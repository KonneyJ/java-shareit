package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

@Data
@AllArgsConstructor
public class Item {
    private int id;

    @NotNull(message = "Поле name не может быть null")
    @NotBlank(message = "Поле name не может быть пустым")
    private String name;

    @NotNull(message = "Поле description не может быть null")
    @NotBlank(message = "Поле description не может быть пустым")
    private String description;

    @NotNull(message = "Поле available не может быть null")
    @NotBlank(message = "Поле available не может быть пустым")
    private boolean available;
    private User owner;
    private ItemRequest request;
}
