package ru.practicum.shareit.bookingTest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private ru.practicum.shareit.booking.controller.BookingController controller;

    private ObjectMapper mapper;
    private MockMvc mvc;
    private BookingResponseDto responseDto;
    private BookingRequestDto requestDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        responseDto = new BookingResponseDto();
        responseDto.setId(10L);
        responseDto.setStart(LocalDateTime.now().plusDays(1));
        responseDto.setEnd(LocalDateTime.now().plusDays(3));

        User booker = new User();
        booker.setId(2L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");

        responseDto.setBooker(booker);
        responseDto.setItem(item);

        requestDto = new BookingRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(3));

        booking = new Booking();
        booking.setId(10L);
        responseDto.setStatus("WAITING");
    }

    @Test
    void createBooking_shouldReturnBooking() throws Exception {
        when(bookingService.createBooking(any(Booking.class), eq(2L))).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(responseDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.item.id", is(1)))
                .andExpect(jsonPath("$.booker.id", is(2)))
                .andExpect(jsonPath("$.status", is("WAITING")));
    }

    @Test
    void findBookingById_shouldReturnBooking() throws Exception {
        when(bookingService.findBookingById(10L, 1L)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(responseDto);

        mvc.perform(get("/bookings/10")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.item.id", is(1)));
    }

    @Test
    void findAllBookingsByUser_shouldReturnList() throws Exception {
        when(bookingService.findAllBookingsByUser(eq(2L), any(BookingState.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(responseDto);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(10)));
    }

    @Test
    void getOwnerBookings_shouldReturnList() throws Exception {
        when(bookingService.getOwnerBookings(eq(1L), any(BookingState.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(responseDto);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(10)));
    }

    @Test
    void updateStatus_shouldApproveBooking() throws Exception {
        BookingResponseDto approvedDto = new BookingResponseDto();
        approvedDto.setId(10L);
        approvedDto.setStatus("APPROVED");

        when(bookingService.updateBookingStatus(10L, 1L, true)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(approvedDto);

        mvc.perform(patch("/bookings/10")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void updateStatus_shouldRejectBooking() throws Exception {
        BookingResponseDto rejectedDto = new BookingResponseDto();
        rejectedDto.setId(10L);
        rejectedDto.setStatus("REJECTED");

        when(bookingService.updateBookingStatus(10L, 1L, false)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(rejectedDto);

        mvc.perform(patch("/bookings/10")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")));
    }

    @Test
    void createBooking_shouldMapDtoToBooking() throws Exception {
        Long userId = 2L;

        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(3));

        Booking booking = new Booking();
        booking.setId(1L);

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);

        when(bookingService.createBooking(any(Booking.class), eq(userId))).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(responseDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingMapper, times(1)).toDto(booking);
    }
}