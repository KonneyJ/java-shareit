package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.item.service.ItemService;
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
public class BookingServiceImplTest {
    private final EntityManager em;
    private final BookingService service;
    private final UserService userService;
    private final ItemService itemService;

    private BookingInputDto bookingInputDto;
    private UserDto userDto;
    private ItemSaveDto itemSaveDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setName("Julie");
        userDto.setEmail("julie17@yandex.ru");

        itemSaveDto = new ItemSaveDto();
        itemSaveDto.setName("name");
        itemSaveDto.setDescription("description");
        itemSaveDto.setAvailable(true);

        bookingInputDto = new BookingInputDto();
        bookingInputDto.setStart(LocalDateTime.now());
        bookingInputDto.setEnd(LocalDateTime.now().plusDays(1));
    }

    @Test
    void createBookingTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);

        service.createBooking(userId, bookingInputDto);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.item.id = :item_id",
                Booking.class);
        Booking booking = query.setParameter("item_id", bookingInputDto.getItemId())
                .getSingleResult();

        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getItem().getId(), equalTo(bookingInputDto.getItemId()));
    }

    @Test
    void createBookingByNotExistingUserTest() {
        int userId = userDto.getId();

        assertThrows(UserNotFoundException.class, () -> service.createBooking(userId, bookingInputDto));
    }

    @Test
    void createBookingByNotExistingItemTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();

        assertThrows(ItemNotFoundException.class, () -> service.createBooking(userId, bookingInputDto));
    }


    @Test
    void getBookingByIdTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        BookingDto booking = service.createBooking(userId, bookingInputDto);

        BookingDto bookingDto = service.getBookingById(userId, booking.getId());

        assertThat(bookingDto.getId(), equalTo(booking.getId()));
        assertThat(bookingDto.getItem().getId(), equalTo(bookingInputDto.getItemId()));
    }

    @Test
    void getBookingByIdByNotExistingUserTest() {
        int userId = userDto.getId();

        assertThrows(UserNotFoundException.class, () -> service.getBookingById(userId, bookingInputDto.getId()));
    }

    @Test
    void getBookingByIdByNotExistingBookingTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);

        assertThrows(BookingNotFoundException.class, () -> service.getBookingById(userId, bookingInputDto.getId()));
    }

    @Test
    void getAllBookingsByUserTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        BookingDto booking = service.createBooking(userId, bookingInputDto);
        List<BookingDto> sourceBookings = List.of(booking);

        Collection<BookingDto> targetBookings = service.getAllBookingsByUser(userId, BookingState.ALL);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
    }
}
