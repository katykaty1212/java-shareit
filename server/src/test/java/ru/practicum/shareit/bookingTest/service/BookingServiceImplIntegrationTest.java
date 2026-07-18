package ru.practicum.shareit.bookingTest.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserState;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        classes = ShareItServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EntityManager em;

    private User owner;
    private User booker;
    private Item availableItem;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .name("Владелец")
                .email("owner@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(owner);

        booker = User.builder()
                .name("Арендатор")
                .email("booker@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(booker);

        availableItem = new Item();
        availableItem.setName("Дрель");
        availableItem.setDescription("Аккумуляторная дрель");
        availableItem.setAvailable(true);
        availableItem.setOwner(owner);
        em.persist(availableItem);

        em.flush();
    }

    @Test
    void createBooking_shouldSaveAndReturnBooking() {
        Booking newBooking = new Booking();
        newBooking.setItem(availableItem);
        newBooking.setStart(LocalDateTime.now().plusDays(1));
        newBooking.setEnd(LocalDateTime.now().plusDays(3));

        Booking result = bookingService.createBooking(newBooking, booker.getId());

        assertThat(result.getId(), notNullValue());
        assertThat(result.getItem().getId(), is(availableItem.getId()));
        assertThat(result.getBooker().getId(), is(booker.getId()));
        assertThat(result.getStatus(), is(BookingStatus.WAITING));

        Booking savedBooking = em.find(Booking.class, result.getId());
        assertThat(savedBooking, notNullValue());
        assertThat(savedBooking.getStatus(), is(BookingStatus.WAITING));
    }

    @Test
    void findBookingById_shouldReturnBooking() {
        Booking booking = createTestBooking(BookingStatus.WAITING);

        Booking result = bookingService.findBookingById(booking.getId(), booker.getId());

        assertThat(result.getId(), is(booking.getId()));
        assertThat(result.getItem().getId(), is(availableItem.getId()));
        assertThat(result.getBooker().getId(), is(booker.getId()));
    }

    @Test
    void updateBookingStatus_shouldApproveBooking() {
        Booking booking = createTestBooking(BookingStatus.WAITING);

        Booking result = bookingService.updateBookingStatus(booking.getId(), owner.getId(), true);

        assertThat(result.getStatus(), is(BookingStatus.APPROVED));

        Booking updatedBooking = em.find(Booking.class, booking.getId());
        assertThat(updatedBooking.getStatus(), is(BookingStatus.APPROVED));
    }

    @Test
    void updateBookingStatus_shouldRejectBooking() {
        Booking booking = createTestBooking(BookingStatus.WAITING);

        Booking result = bookingService.updateBookingStatus(booking.getId(), owner.getId(), false);

        assertThat(result.getStatus(), is(BookingStatus.REJECTED));

        Booking updatedBooking = em.find(Booking.class, booking.getId());
        assertThat(updatedBooking.getStatus(), is(BookingStatus.REJECTED));
    }

    @Test
    void findAllBookingsByUser_shouldReturnUserBookings() {
        Booking booking1 = createTestBooking(BookingStatus.APPROVED);
        Booking booking2 = createTestBooking(BookingStatus.WAITING);

        List<Booking> result = bookingService.findAllBookingsByUser(booker.getId(), BookingState.ALL);

        assertThat(result, hasSize(2));
    }

    private Booking createTestBooking(BookingStatus status) {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(3));
        booking.setItem(availableItem);
        booking.setBooker(booker);
        booking.setStatus(status);
        em.persist(booking);
        em.flush();
        return booking;
    }
}