package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        bookingDto = new BookingDto();
        bookingDto.setId(1);
        bookingDto.setStart(LocalDateTime.now());
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));
        bookingDto.setItem(new Item());
        bookingDto.setBooker(new User());
        bookingDto.setStatus(BookingStatus.WAITING);
    }

    @SneakyThrows
    @Test
    void createBookingTest() {
        when(bookingService.createBooking(anyInt(), any(BookingInputDto.class)))
                .thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingDto)))
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Integer.class));

        verify(bookingService, times(1)).createBooking(anyInt(), any(BookingInputDto.class));
    }

    @SneakyThrows
    @Test
    void approveBookingTest() {
        int userId = 1;
        int bookingId = bookingDto.getId();
        boolean approved = true;

        when(bookingService.approveBooking(userId, bookingId, approved))
                .thenAnswer(invocationOnMock -> {
                    bookingDto.setStatus(BookingStatus.APPROVED);
                    return bookingDto;
                });

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", String.valueOf(approved))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(BookingStatus.APPROVED.name()));

        verify(bookingService, times(1)).approveBooking(userId, bookingId, approved);
    }

    @SneakyThrows
    @Test
    void getBookingByIdTest() {
        int bookingId = bookingDto.getId();

        when(bookingService.getBookingById(anyInt(), anyInt())).thenReturn(bookingDto);

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingDto)))
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Integer.class));

        verify(bookingService, times(1)).getBookingById(anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void getAllBookingsByUserTest() {
        Collection<BookingDto> bookings = List.of(bookingDto);
        BookingState state = BookingState.FUTURE;
        int userId = 1;

        when(bookingService.getAllBookingsByUser(userId, state)).thenReturn(bookings);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", String.valueOf(state))
                        .content(mapper.writeValueAsString(bookings))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookings)));

        verify(bookingService, times(1)).getAllBookingsByUser(userId, state);
    }

    @SneakyThrows
    @Test
    void getAllBookingByUserItemsTest() {
        Collection<BookingDto> bookings = List.of(bookingDto);
        BookingState state = BookingState.FUTURE;
        int userId = 1;

        when(bookingService.getAllBookingsByUserItems(userId, state)).thenReturn(bookings);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", String.valueOf(state))
                        .content(mapper.writeValueAsString(bookings))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookings)));

        verify(bookingService, times(1)).getAllBookingsByUserItems(userId, state);
    }
}
