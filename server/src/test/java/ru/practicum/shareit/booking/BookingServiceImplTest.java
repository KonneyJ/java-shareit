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
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.*;
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
        bookingInputDto.setStart(LocalDateTime.now().minusDays(1));
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
    void createBookingByNotAvailableItemTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        itemSaveDto.setAvailable(false);
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);

        assertThrows(ConditionException.class, () -> service.createBooking(userId, bookingInputDto));
    }

    @Test
    void approveBookingTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        BookingDto savedBooking = service.createBooking(userId, bookingInputDto);
        int bookingId = savedBooking.getId();

        BookingDto booking = service.approveBooking(userId, bookingId, true);

        assertThat(booking.getId(), equalTo(bookingId));
        assertThat(booking.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void approveBookingByNotExistingUserTest() {
        int userId = userDto.getId();
        int bookingId = bookingInputDto.getId();

        assertThrows(ForbiddenException.class, () -> service.approveBooking(userId, bookingId, true));
    }

    @Test
    void approveNotExistingBookingTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        int bookingId = bookingInputDto.getId();

        assertThrows(BookingNotFoundException.class, () -> service.approveBooking(userId, bookingId, true));
    }

    @Test
    void approveBookingByNotOwnerItemTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        UserDto userDto2 = new UserDto();
        userDto2.setName("name");
        userDto2.setEmail("email@yandex.ru");
        UserDto otherUser = userService.createUser(userDto2);
        BookingDto savedBooking = service.createBooking(userId, bookingInputDto);
        int bookingId = savedBooking.getId();

        assertThrows(ForbiddenException.class, () -> service.approveBooking(otherUser.getId(), bookingId, true));
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
    void getBookingByIdByOtherUserTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        BookingDto booking = service.createBooking(userId, bookingInputDto);
        int bookingId = booking.getId();

        UserDto userDto2 = new UserDto();
        userDto2.setName("name");
        userDto2.setEmail("email@yandex.ru");
        UserDto otherUser = userService.createUser(userDto2);
        int userId2 = otherUser.getId();

        assertThrows(ConditionException.class, () -> service.getBookingById(userId2, bookingId));
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

    @Test
    void getAllBookingsByUserStateCurrentTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        BookingDto booking = service.createBooking(userId, bookingInputDto);
        List<BookingDto> sourceBookings = List.of(booking);

        Collection<BookingDto> targetBookings = service.getAllBookingsByUser(userId, BookingState.CURRENT);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
    }

    @Test
    void getAllBookingsByUserStateFutureTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        bookingInputDto.setStart(LocalDateTime.now().plusDays(10));
        bookingInputDto.setEnd(LocalDateTime.now().plusDays(15));
        BookingDto booking = service.createBooking(userId, bookingInputDto);
        List<BookingDto> sourceBookings = List.of(booking);

        Collection<BookingDto> targetBookings = service.getAllBookingsByUser(userId, BookingState.FUTURE);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
    }

    @Test
    void getAllBookingsByUserStatePastTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        bookingInputDto.setStart(LocalDateTime.now().minusDays(15));
        bookingInputDto.setEnd(LocalDateTime.now().minusDays(10));
        BookingDto booking = service.createBooking(userId, bookingInputDto);
        List<BookingDto> sourceBookings = List.of(booking);

        Collection<BookingDto> targetBookings = service.getAllBookingsByUser(userId, BookingState.PAST);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
    }

    @Test
    void getAllBookingsByUserWaitingStatusTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        BookingDto booking = service.createBooking(userId, bookingInputDto);
        List<BookingDto> sourceBookings = List.of(booking);

        Collection<BookingDto> targetBookings = service.getAllBookingsByUser(userId, BookingState.WAITING);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
    }

    @Test
    void getAllBookingByUserRejectedStatusTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        BookingDto booking = service.createBooking(userId, bookingInputDto);
        int bookingId = booking.getId();
        BookingDto approvedBooking = service.approveBooking(userId, bookingId, false);
        List<BookingDto> sourceBookings = List.of(approvedBooking);

        Collection<BookingDto> targetBookings = service.getAllBookingsByUser(userId, BookingState.REJECTED);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
    }

    @Test
    void getAllBookingsByUserWrongStateTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        BookingState state = BookingState.TEST;

        assertThrows(ConditionException.class, () -> service.getAllBookingsByUser(userId, state));
    }

    @Test
    void getAllBookingsByNotExistingUserTest() {
        int userId = userDto.getId();

        assertThrows(UserNotFoundException.class, () -> service.getAllBookingsByUser(userId, BookingState.ALL));
    }

    @Test
    void getAllBookingByUserItemsTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        BookingDto booking = service.createBooking(userId, bookingInputDto);
        List<BookingDto> sourceBookings = List.of(booking);

        Collection<BookingDto> targetBookings = service.getAllBookingsByUserItems(userId, BookingState.ALL);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
    }

    @Test
    void getAllBookingByUserItemsStateFutureTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        bookingInputDto.setStart(LocalDateTime.now().plusDays(10));
        bookingInputDto.setEnd(LocalDateTime.now().plusDays(15));
        BookingDto booking = service.createBooking(userId, bookingInputDto);
        List<BookingDto> sourceBookings = List.of(booking);

        Collection<BookingDto> targetBookings = service.getAllBookingsByUserItems(userId, BookingState.FUTURE);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
    }

    @Test
    void getAllBookingByUserItemsStatePastTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        bookingInputDto.setStart(LocalDateTime.now().minusDays(15));
        bookingInputDto.setEnd(LocalDateTime.now().minusDays(10));
        BookingDto booking = service.createBooking(userId, bookingInputDto);
        List<BookingDto> sourceBookings = List.of(booking);

        Collection<BookingDto> targetBookings = service.getAllBookingsByUserItems(userId, BookingState.PAST);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
    }

    @Test
    void getAllBookingByUserItemsWaitingStatusTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        BookingDto booking = service.createBooking(userId, bookingInputDto);
        List<BookingDto> sourceBookings = List.of(booking);

        Collection<BookingDto> targetBookings = service.getAllBookingsByUserItems(userId, BookingState.WAITING);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
    }

    @Test
    void getAllBookingByUserItemsRejectedStatusTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        int itemId = item.getId();
        bookingInputDto.setItemId(itemId);
        BookingDto booking = service.createBooking(userId, bookingInputDto);
        int bookingId = booking.getId();
        BookingDto approvedBooking = service.approveBooking(userId, bookingId, false);
        List<BookingDto> sourceBookings = List.of(approvedBooking);

        Collection<BookingDto> targetBookings = service.getAllBookingsByUserItems(userId, BookingState.REJECTED);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
    }

    @Test
    void getAllBookingsByUserItemsWrongStateTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemDto item = itemService.createItem(userId, itemSaveDto);
        BookingState state = BookingState.TEST;

        assertThrows(ConditionException.class, () -> service.getAllBookingsByUserItems(userId, state));
    }

    @Test
    void getAllBookingsByNotExistingUserItemsTest() {
        int userId = userDto.getId();

        assertThrows(UserNotFoundException.class, () -> service.getAllBookingsByUserItems(userId, BookingState.ALL));
    }

    @Test
    void getAllBookingByUserItemsWithoutItemsTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();

        assertThrows(ItemNotFoundException.class, () -> service.getAllBookingsByUserItems(userId, BookingState.ALL));
    }
}
