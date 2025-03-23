package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookingMapperTest {
    @Test
    void bookingMapperTest() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1);
        bookingDto.setStatus(BookingStatus.APPROVED);

        Booking booking = BookingMapper.toBooking(bookingDto);

        assertEquals(booking.getId(), bookingDto.getId());
        assertEquals(booking.getStatus(), bookingDto.getStatus());
    }
}
