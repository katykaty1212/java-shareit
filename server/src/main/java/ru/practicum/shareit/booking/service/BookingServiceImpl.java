package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    public Booking findBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Не найдена вещь при создании бронирования."));

        if (!booking.getItem().getOwner().getId().equals(userId)
                && !booking.getBooker().getId().equals(userId)) {
            throw new NotFoundException("Бронирование не найдено");
        }

        return booking;
    }

    @Override
    public List<Booking> findAllBookingsByUser(Long userId, BookingState state) {
        List<Booking> bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
        return filterBookings(bookings, state);
    }

    public List<Booking> getOwnerBookings(Long ownerId, BookingState state) {
        if (itemRepository.findAllByOwnerId(ownerId).isEmpty()) {
            throw new RuntimeException("У пользователя нет вещей");
        }

        List<Booking> bookings = bookingRepository.findAllByOwnerIdOrderByStartDesc(ownerId);
        return filterBookings(bookings, state);
    }

    @Override
    public Booking createBooking(Booking booking, Long bookerId) {
        User booker = userService.getUserById(bookerId);
        Item item = itemRepository.findById(booking.getItem().getId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена."));

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException("Владелец не может забронировать свою вещь");
        }

        if (!booking.getStart().isBefore(booking.getEnd())) {
            throw new IllegalArgumentException("Дата начала должна быть раньше даты окончания");
        }

        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);
        log.info("Бронирование сохранено с ID: {}", saved.getId());
        return saved;
    }

    @Override
    public Booking updateBookingStatus(Long bookingId, Long ownerId, boolean approved) {
        log.info("Обновление статуса: bookingId={}, ownerId={}, approved={}", bookingId, ownerId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена."));
        log.info("Бронирование найдено: {}", booking.getId());

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            log.warn("Доступ запрещен: ownerId={}, владелец вещи={}", ownerId, booking.getItem().getOwner().getId());
            throw new AccessDeniedException("Статус может менять только владелец вещи.");
        }
        log.info("Владелец подтвержден");

        if (booking.getStatus() != BookingStatus.WAITING) {
            log.warn("Статус не WAITING: {}", booking.getStatus());
            throw new IllegalArgumentException("Статус бронирования: " + booking.getStatus());
        }
        log.info("Статус WAITING, можно обновлять");

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        log.info("Новый статус: {}", booking.getStatus());

        Booking saved = bookingRepository.save(booking);
        log.info("Бронирование сохранено с ID: {}", saved.getId());
        return saved;
    }

    private List<Booking> filterBookings(List<Booking> bookings, BookingState state) {
        LocalDateTime now = LocalDateTime.now();

        return switch (state) {
            case CURRENT -> bookings.stream()
                    .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                    .toList();
            case PAST -> bookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .toList();
            case FUTURE -> bookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .toList();
            case WAITING -> bookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.WAITING)
                    .toList();
            case REJECTED -> bookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.REJECTED)
                    .toList();
            case ALL -> bookings;
        };
    }
}
