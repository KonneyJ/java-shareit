package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ConditionException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        properties = "jdbc.url=jdbc:postgresql://localhost:5432/test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplTest {
    private final EntityManager em;
    private final ItemService service;
    private final UserService userService;

    private ItemSaveDto itemSaveDto;
    private ItemDto itemDto;
    private UserDto userDto;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setName("Julie");
        userDto.setEmail("julie17@yandex.ru");

        itemSaveDto = new ItemSaveDto();
        itemSaveDto.setName("name");
        itemSaveDto.setDescription("description");
        itemSaveDto.setAvailable(true);

        itemDto = new ItemDto();
        itemDto.setName("nameDto");
        itemDto.setDescription("descriptionDto");
        itemDto.setAvailable(true);

        commentDto = new CommentDto();
        commentDto.setText("comment");
        commentDto.setAuthorName(userDto.getName());
    }

    @Test
    void createItemTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();

        service.createItem(userId, itemSaveDto);

        TypedQuery<Item> query = em.createQuery("Select it from Item it where it.description = :description",
                Item.class);
        Item item = query.setParameter("description", itemSaveDto.getDescription())
                .getSingleResult();

        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemSaveDto.getName()));
        assertThat(item.getDescription(), equalTo(itemSaveDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemSaveDto.getAvailable()));
    }

    @Test
    void createItemByUserNotExistingTest() {
        int userId = userDto.getId();

        assertThrows(UserNotFoundException.class, () -> service.createItem(userId, itemSaveDto));
    }

    @Test
    void updateItemTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto savedItem = service.createItem(userId, itemSaveDto);
        int itemId = savedItem.getId();
        service.updateItem(userId, itemId, itemDto);

        TypedQuery<Item> query = em.createQuery("Select it from Item it where it.id = :item_id",
                Item.class);
        Item item = query.setParameter("item_id", itemId)
                .getSingleResult();

        assertThat(item.getId(), notNullValue());
        assertThat(item.getId(), equalTo(itemId));
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemDto.getAvailable()));
    }

    @Test
    void updateItemByNotExistingIdTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        int itemId = itemDto.getId();

        assertThrows(ItemNotFoundException.class, () -> service.updateItem(userId, itemId, itemDto));
    }

    @Test
    void getItemByIdTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();

        ItemDto item = service.createItem(userId, itemSaveDto);

        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemSaveDto.getName()));
        assertThat(item.getDescription(), equalTo(itemSaveDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemSaveDto.getAvailable()));
    }

    @Test
    void getItemByNotExistingIdTest() {
        UserDto user = userService.createUser(userDto);
        int itemId = itemDto.getId();

        assertThrows(ItemNotFoundException.class, () -> service.getItemById(itemId));
    }

    @Test
    void getAllItemsTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        service.createItem(userId, itemSaveDto);
        List<ItemSaveDto> sourceItems = List.of(itemSaveDto);

        Collection<ItemDto> targetItems = service.getAllItems(userId);

        assertThat(targetItems, hasSize(sourceItems.size()));
        for (ItemSaveDto sourceItem : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItem.getName())),
                    hasProperty("description", equalTo(sourceItem.getDescription()))
            )));
        }
    }

    @Test
    void getAllItemsByUserNotExistingTest() {
        int userId = userDto.getId();

        assertThrows(UserNotFoundException.class, () -> service.getAllItems(userId));
    }

    @Test
    void searchItemsTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        service.createItem(userId, itemSaveDto);
        Collection<ItemSaveDto> sourceItems = List.of(itemSaveDto);

        Collection<ItemDto> targetItems = service.searchItems(itemSaveDto.getDescription());

        assertThat(targetItems, hasSize(sourceItems.size()));
        for (ItemSaveDto sourceItem : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItem.getName())),
                    hasProperty("description", equalTo(sourceItem.getDescription()))
            )));
        }
    }

    @Test
    void searchItemsWithEmptyTextTest() {
        Collection<ItemSaveDto> sourceItems = List.of();
        String text = "";

        Collection<ItemDto> targetItems = service.searchItems(text);

        assertThat(targetItems, hasSize(sourceItems.size()));
    }

    @Test
    void addCommentTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = service.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(em.find(Item.class, itemId))
                .booker(em.find(User.class, userId))
                .status(BookingStatus.APPROVED)
                .build();
        em.persist(booking);

        service.addComment(userId, itemId, commentDto);

        TypedQuery<Comment> query = em.createQuery("Select c from Comment c where c.text = :text",
                Comment.class);
        Comment comment = query.setParameter("text", commentDto.getText())
                .getSingleResult();

        assertThat(comment.getId(), notNullValue());
        assertThat(comment.getUser().getName(), equalTo(commentDto.getAuthorName()));
        assertThat(comment.getText(), equalTo(commentDto.getText()));
    }

    @Test
    void addCommentWithoutBookingTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = service.createItem(userId, itemSaveDto);
        int itemId = item.getId();

        assertThrows(ConditionException.class, () -> service.addComment(userId, itemId, commentDto));
    }

    @Test
    void addCommentWithoutItemTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        int itemId = itemDto.getId();

        assertThrows(ItemNotFoundException.class, () -> service.addComment(userId, itemId, commentDto));
    }

    @Test
    void addCommentWithoutUserTest() {
        int userId = userDto.getId();
        int itemId = itemDto.getId();

        assertThrows(UserNotFoundException.class, () -> service.addComment(userId, itemId, commentDto));
    }

    @Test
    void getItemsByRequestIdTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemRequest request = ItemRequest.builder()
                .description("description")
                .requester(em.find(User.class, userId))
                .build();
        em.persist(request);
        itemSaveDto.setRequestId(request.getId());
        service.createItem(userId, itemSaveDto);
        Collection<ItemSaveDto> sourceItems = List.of(itemSaveDto);

        Collection<ItemResponseDto> targetItems = service.getItemsByRequestId(request.getId());

        assertThat(targetItems, hasSize(sourceItems.size()));
    }
}
