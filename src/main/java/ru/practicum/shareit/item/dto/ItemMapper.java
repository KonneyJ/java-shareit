package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

@Component("itemMapper")
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                String.valueOf(item.isAvailable()),
                item.getOwner(),
                item.getRequest()
        );
    }

    public static Item toItem(ItemDto itemDto) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                Boolean.parseBoolean(itemDto.getAvailable()),
                itemDto.getOwner(),
                itemDto.getRequest()
        );
    }
}
