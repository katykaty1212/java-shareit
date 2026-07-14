package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @GetMapping("/{bookingId}")
    public BookingResponseDto findBookingById(@PathVariable Long bookingId,
                                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingMapper.toDto(bookingService.findBookingById(bookingId, userId));
    }

    @GetMapping
    public List<BookingResponseDto> findAllBookingsByUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                          @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.findAllBookingsByUser(userId, state).stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                     @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.getOwnerBookings(ownerId, state).stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public BookingResponseDto createBooking(@RequestBody BookingRequestDto dto,
                                            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Создание бронирования: itemId={}, userId={}", dto.getItemId(), userId);

        Item item = new Item();
        item.setId(dto.getItemId());

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());

        Booking created = bookingService.createBooking(booking, userId);
        BookingResponseDto response = bookingMapper.toDto(created);

        log.info("Ответ: booker.id={}, item.id={}",
                response.getBooker() != null ? response.getBooker().getId() : "null",
                response.getItem() != null ? response.getItem().getId() : "null");

        return response;
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateStatus(@PathVariable Long bookingId,
                                           @RequestHeader("X-Sharer-User-Id") Long ownerId,
                                           @RequestParam boolean approved) {
        log.info("=== PATCH ЗАПРОС ===");
        log.info("bookingId={}, ownerId={}, approved={}", bookingId, ownerId, approved);

        Booking updated = bookingService.updateBookingStatus(bookingId, ownerId, approved);
        BookingResponseDto response = bookingMapper.toDto(updated);

        log.info("Ответ: статус={}", response.getStatus());
        return response;
    }
}