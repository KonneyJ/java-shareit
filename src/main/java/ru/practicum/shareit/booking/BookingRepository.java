package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    Collection<Booking> findAllByBookerIdOrderByStartDesc(int bookerId);

    @Query("select b from Booking as b " +
            "where b.booker.id = ?1 " +
            "and ?2 > b.start " +
            "and ?2 < b.end")
    Collection<Booking> findAllBookingsCurrent(int bookerId, LocalDateTime current);

    Collection<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(int bookerId, LocalDateTime time);

    Collection<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(int bookerId, LocalDateTime time);

    Collection<Booking> findAllByBookerIdAndStatusOrderByStartDesc(int bookerId, BookingStatus status);

    Collection<Booking> findAllByItemIdInOrderByStartDesc(Collection<Integer> ids);

    @Query("select b from Booking as b " +
            "where b.booker.id in ?1 " +
            "and ?2 > b.start " +
            "and ?2 < b.end")
    Collection<Booking> findAllBookingsCurrent(Collection<Integer> bookerId, LocalDateTime current);

    Collection<Booking> findAllByItemIdInAndEndBeforeOrderByStartDesc(Collection<Integer> ids,
                                                                      LocalDateTime time);

    Collection<Booking> findAllByItemIdInAndStartAfterOrderByStartDesc(Collection<Integer> ids,
                                                                       LocalDateTime time);

    Collection<Booking> findAllByItemIdInAndStatusOrderByStartDesc(Collection<Integer> ids, BookingStatus status);

    Booking findByBookerIdAndItemIdAndEndBeforeOrderByStartDesc(int userId, int itemId, LocalDateTime current);

    Collection<Booking> findAllByItemIdAndStartAfterOrderByStartAsc(int itemId, LocalDateTime time);

    Collection<Booking> findAllByItemIdAndEndBeforeOrderByEndAsc(int itemId, LocalDateTime time);
}
