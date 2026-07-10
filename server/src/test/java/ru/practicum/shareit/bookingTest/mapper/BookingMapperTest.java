package ru.practicum.shareit.bookingTest.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    private BookingMapper mapper;
    private BookingRequestDto requestDto;
    private Item item;
    private User booker;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(BookingMapper.class);

        requestDto = new BookingRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.of(2025, 12, 25, 10, 0));
        requestDto.setEnd(LocalDateTime.of(2025, 12, 27, 18, 0));

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");

        booker = new User();
        booker.setId(2L);
        booker.setName("Иван");
    }

    @Test
    void toBooking_shouldMapDtoAndItemAndBookerToBooking() {
        Booking booking = mapper.toBooking(requestDto, item, booker);

        assertNotNull(booking);
        assertNull(booking.getId());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
        assertEquals(requestDto.getStart(), booking.getStart());
        assertEquals(requestDto.getEnd(), booking.getEnd());
    }

    @Test
    void toDto_shouldMapBookingToResponseDto() {
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setStart(requestDto.getStart());
        booking.setEnd(requestDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);

        BookingResponseDto response = mapper.toDto(booking);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getItemId());
        assertEquals(2L, response.getBookerId());
        assertEquals("APPROVED", response.getStatus());
        assertEquals(requestDto.getStart(), response.getStart());
        assertEquals(requestDto.getEnd(), response.getEnd());
    }

    @Test
    void toDto_shouldHandleNullBooking() {
        BookingResponseDto response = mapper.toDto(null);
        assertNull(response);
    }

    @Test
    void toBooking_shouldMapAllFields() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(3));

        Item item = new Item();
        item.setId(1L);

        User booker = new User();
        booker.setId(2L);

        Booking booking = mapper.toBooking(dto, item, booker);

        assertNotNull(booking);
        assertNull(booking.getId());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
        assertEquals(dto.getStart(), booking.getStart());
        assertEquals(dto.getEnd(), booking.getEnd());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
    }

    @Test
    void toDto_shouldMapAllFields() {
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(3));

        Item item = new Item();
        item.setId(1L);
        booking.setItem(item);

        User booker = new User();
        booker.setId(2L);
        booking.setBooker(booker);

        booking.setStatus(BookingStatus.APPROVED);

        BookingResponseDto dto = mapper.toDto(booking);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals(1L, dto.getItemId());
        assertEquals(2L, dto.getBookerId());
        assertEquals("APPROVED", dto.getStatus());
        assertEquals(booking.getStart(), dto.getStart());
        assertEquals(booking.getEnd(), dto.getEnd());
    }

    @Test
    void toBooking_shouldHandleNullItem() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(3));

        Booking booking = mapper.toBooking(dto, null, new User());

        assertNotNull(booking);
        assertNull(booking.getItem());
    }

    @Test
    void toBooking_shouldHandleNullBooker() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(3));

        Booking booking = mapper.toBooking(dto, new Item(), null);

        assertNotNull(booking);
        assertNull(booking.getBooker());
    }
}