package ru.practicum.shareit.bookingTest.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
class BookingRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void save_shouldGenerateId() {
        User owner = User.builder()
                .name("Владелец")
                .email("owner@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(owner);

        User booker = User.builder()
                .name("Арендатор")
                .email("booker@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(booker);

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Аккумуляторная");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(3));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);

        assertThat(saved.getId(), notNullValue());
        assertThat(saved.getStatus(), is(BookingStatus.WAITING));
        assertThat(saved.getItem().getId(), is(item.getId()));
        assertThat(saved.getBooker().getId(), is(booker.getId()));
    }

    @Test
    void findById_shouldReturnBooking() {
        User owner = User.builder()
                .name("Владелец2")
                .email("owner2@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(owner);

        User booker = User.builder()
                .name("Арендатор2")
                .email("booker2@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(booker);

        Item item = new Item();
        item.setName("Перфоратор");
        item.setDescription("Мощный");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(3));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        em.persist(booking);
        em.flush();

        Booking found = bookingRepository.findById(booking.getId()).orElse(null);

        assertThat(found, notNullValue());
        assertThat(found.getId(), is(booking.getId()));
        assertThat(found.getItem().getId(), is(item.getId()));
        assertThat(found.getBooker().getId(), is(booker.getId()));
    }

    @Test
    void findAllByBookerIdOrderByStartDesc_shouldReturnBookerBookings() {
        User owner = User.builder()
                .name("Владелец3")
                .email("owner3@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(owner);

        User booker = User.builder()
                .name("Арендатор3")
                .email("booker3@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(booker);

        Item item = new Item();
        item.setName("Болгарка");
        item.setDescription("УШМ");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Booking booking1 = new Booking();
        booking1.setStart(LocalDateTime.now().plusDays(1));
        booking1.setEnd(LocalDateTime.now().plusDays(2));
        booking1.setItem(item);
        booking1.setBooker(booker);
        booking1.setStatus(BookingStatus.APPROVED);
        em.persist(booking1);

        Booking booking2 = new Booking();
        booking2.setStart(LocalDateTime.now().plusDays(3));
        booking2.setEnd(LocalDateTime.now().plusDays(5));
        booking2.setItem(item);
        booking2.setBooker(booker);
        booking2.setStatus(BookingStatus.WAITING);
        em.persist(booking2);

        em.flush();

        List<Booking> result = bookingRepository.findAllByBookerIdOrderByStartDesc(booker.getId());

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getBooker().getId(), is(booker.getId()));
    }

    @Test
    void findAllByOwnerIdOrderByStartDesc_shouldReturnOwnerBookings() {
        User owner = User.builder()
                .name("Владелец4")
                .email("owner4@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(owner);

        User booker = User.builder()
                .name("Арендатор4")
                .email("booker4@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(booker);

        Item item = new Item();
        item.setName("Лобзик");
        item.setDescription("Электрический");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Booking booking1 = new Booking();
        booking1.setStart(LocalDateTime.now().plusDays(1));
        booking1.setEnd(LocalDateTime.now().plusDays(2));
        booking1.setItem(item);
        booking1.setBooker(booker);
        booking1.setStatus(BookingStatus.APPROVED);
        em.persist(booking1);

        Booking booking2 = new Booking();
        booking2.setStart(LocalDateTime.now().plusDays(3));
        booking2.setEnd(LocalDateTime.now().plusDays(5));
        booking2.setItem(item);
        booking2.setBooker(booker);
        booking2.setStatus(BookingStatus.WAITING);
        em.persist(booking2);

        em.flush();

        List<Booking> result = bookingRepository.findAllByOwnerIdOrderByStartDesc(owner.getId());

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getItem().getOwner().getId(), is(owner.getId()));
    }

    @Test
    void findFirstByItemIdAndEndBeforeOrderByEndDesc_shouldReturnLastBooking() {
        User owner = User.builder()
                .name("Владелец5")
                .email("owner5@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(owner);

        User booker = User.builder()
                .name("Арендатор5")
                .email("booker5@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(booker);

        Item item = new Item();
        item.setName("Шуруповерт");
        item.setDescription("Аккумуляторный");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        LocalDateTime now = LocalDateTime.now();

        Booking pastBooking = new Booking();
        pastBooking.setStart(now.minusDays(3));
        pastBooking.setEnd(now.minusDays(1));
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        em.persist(pastBooking);

        Booking pastBooking2 = new Booking();
        pastBooking2.setStart(now.minusDays(5));
        pastBooking2.setEnd(now.minusDays(4));
        pastBooking2.setItem(item);
        pastBooking2.setBooker(booker);
        pastBooking2.setStatus(BookingStatus.APPROVED);
        em.persist(pastBooking2);

        em.flush();

        Optional<Booking> result = bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(item.getId(), now);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getEnd(), is(pastBooking.getEnd()));
    }

    @Test
    void findFirstByItemIdAndStartAfterOrderByStartAsc_shouldReturnNextBooking() {
        User owner = User.builder()
                .name("Владелец6")
                .email("owner6@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(owner);

        User booker = User.builder()
                .name("Арендатор6")
                .email("booker6@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(booker);

        Item item = new Item();
        item.setName("Бензопила");
        item.setDescription("Мощная");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        LocalDateTime now = LocalDateTime.now();

        Booking futureBooking1 = new Booking();
        futureBooking1.setStart(now.plusDays(1));
        futureBooking1.setEnd(now.plusDays(3));
        futureBooking1.setItem(item);
        futureBooking1.setBooker(booker);
        futureBooking1.setStatus(BookingStatus.WAITING);
        em.persist(futureBooking1);

        Booking futureBooking2 = new Booking();
        futureBooking2.setStart(now.plusDays(5));
        futureBooking2.setEnd(now.plusDays(7));
        futureBooking2.setItem(item);
        futureBooking2.setBooker(booker);
        futureBooking2.setStatus(BookingStatus.WAITING);
        em.persist(futureBooking2);

        em.flush();

        Optional<Booking> result = bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(item.getId(), now);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getStart(), is(futureBooking1.getStart()));
    }

    @Test
    void findFirstByItemIdAndBookerIdAndEndBefore_shouldReturnBooking_whenUserBookedItem() {
        User owner = User.builder()
                .name("Владелец7")
                .email("owner7@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(owner);

        User booker = User.builder()
                .name("Арендатор7")
                .email("booker7@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(booker);

        User otherUser = User.builder()
                .name("Другой")
                .email("other@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(otherUser);

        Item item = new Item();
        item.setName("Отвертка");
        item.setDescription("Крестовая");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        LocalDateTime now = LocalDateTime.now();

        Booking pastBooking = new Booking();
        pastBooking.setStart(now.minusDays(3));
        pastBooking.setEnd(now.minusDays(1));
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        em.persist(pastBooking);

        em.flush();

        Optional<Booking> result = bookingRepository.findFirstByItemIdAndBookerIdAndEndBefore(
                item.getId(), booker.getId(), now);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getBooker().getId(), is(booker.getId()));
    }

    @Test
    void findFirstByItemIdAndBookerIdAndEndBefore_shouldReturnEmpty_whenUserNotBooked() {
        User owner = User.builder()
                .name("Владелец8")
                .email("owner8@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(owner);

        User booker = User.builder()
                .name("Арендатор8")
                .email("booker8@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(booker);

        Item item = new Item();
        item.setName("Пила");
        item.setDescription("Цепная");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        em.flush();

        Optional<Booking> result = bookingRepository.findFirstByItemIdAndBookerIdAndEndBefore(
                item.getId(), booker.getId(), LocalDateTime.now());

        assertThat(result.isPresent(), is(false));
    }
}