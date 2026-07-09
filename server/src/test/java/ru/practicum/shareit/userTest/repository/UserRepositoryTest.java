package ru.practicum.shareit.userTest.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserState;
import ru.practicum.shareit.user.repository.UserRepository;

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
class UserRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_shouldGenerateId() {
        User user = User.builder()
                .name("Иван")
                .email("ivan@mail.com")
                .state(UserState.ACTIVE)
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getId(), notNullValue());
        assertThat(saved.getName(), is("Иван"));
        assertThat(saved.getEmail(), is("ivan@mail.com"));
    }

    @Test
    void findById_shouldReturnUser() {
        User user = User.builder()
                .name("Петр")
                .email("petr@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(user);
        em.flush();

        User found = userRepository.findById(user.getId()).orElse(null);

        assertThat(found, notNullValue());
        assertThat(found.getName(), is("Петр"));
        assertThat(found.getEmail(), is("petr@mail.com"));
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        User user1 = User.builder()
                .name("Анна")
                .email("anna@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(user1);

        User user2 = User.builder()
                .name("Сергей")
                .email("sergey@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(user2);

        em.flush();

        List<User> result = userRepository.findAll();

        assertThat(result, hasSize(2));
    }

    @Test
    void deleteById_shouldDeleteUser() {
        User user = User.builder()
                .name("Ольга")
                .email("olga@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(user);
        em.flush();

        userRepository.deleteById(user.getId());

        User deleted = userRepository.findById(user.getId()).orElse(null);
        assertThat(deleted, nullValue());
    }

    @Test
    void findByEmail_shouldReturnUser() {
        User user = User.builder()
                .name("Дмитрий")
                .email("dmitry@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(user);
        em.flush();

        Optional<User> found = userRepository.findByEmail("dmitry@mail.com");

        assertThat(found.isPresent(), is(true));
        assertThat(found.get().getName(), is("Дмитрий"));
        assertThat(found.get().getEmail(), is("dmitry@mail.com"));
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("notexist@mail.com");

        assertThat(found.isPresent(), is(false));
    }
}