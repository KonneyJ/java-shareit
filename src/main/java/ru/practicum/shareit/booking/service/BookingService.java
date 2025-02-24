package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.Collection;

public interface BookingService {

    BookingDto createBooking(int userId, BookingInputDto booking);

    BookingDto approveBooking(int userId, int bookingId, boolean approved);

    BookingDto getBookingById(int userId, int bookingId);

    Collection<BookingDto> getAllBookingsByUser(int userId, BookingState state);

    Collection<BookingDto> getAllBookingsByUserItems(int userId, BookingState state);
}
