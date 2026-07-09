package ru.practicum.shareit.itemTest.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserState;

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
class ItemRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findAllByOwnerId_shouldReturnUserItems() {
        User user = User.builder()
                .name("Иван")
                .email("ivan@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(user);
        em.flush();

        Item item1 = new Item();
        item1.setName("Дрель");
        item1.setDescription("Аккумуляторная");
        item1.setAvailable(true);
        item1.setOwner(user);
        em.persist(item1);

        Item item2 = new Item();
        item2.setName("Молоток");
        item2.setDescription("Тяжелый");
        item2.setAvailable(true);
        item2.setOwner(user);
        em.persist(item2);

        em.flush();

        List<Item> result = itemRepository.findAllByOwnerId(user.getId());

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getName(), is("Дрель"));
        assertThat(result.get(1).getName(), is("Молоток"));
    }

    @Test
    void save_shouldGenerateId() {
        User user = User.builder()
                .name("Петр")
                .email("petr@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(user);
        em.flush();

        Item item = new Item();
        item.setName("Перфоратор");
        item.setDescription("Мощный");
        item.setAvailable(true);
        item.setOwner(user);

        Item saved = itemRepository.save(item);

        assertThat(saved.getId(), notNullValue());
        assertThat(saved.getName(), is("Перфоратор"));
        assertThat(saved.getOwner().getId(), is(user.getId()));
    }

    @Test
    void findById_shouldReturnItem() {
        User user = User.builder()
                .name("Сергей")
                .email("sergey@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(user);
        em.flush();

        Item item = new Item();
        item.setName("Болгарка");
        item.setDescription("УШМ");
        item.setAvailable(true);
        item.setOwner(user);
        em.persist(item);
        em.flush();

        Item found = itemRepository.findById(item.getId()).orElse(null);

        assertThat(found, notNullValue());
        assertThat(found.getName(), is("Болгарка"));
    }

    @Test
    void delete_shouldDeleteItem() {
        User user = User.builder()
                .name("Анна")
                .email("anna@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(user);
        em.flush();

        Item item = new Item();
        item.setName("Удаляемая вещь");
        item.setDescription("Будет удалена");
        item.setAvailable(true);
        item.setOwner(user);
        em.persist(item);
        em.flush();

        itemRepository.deleteById(item.getId());

        Item deleted = itemRepository.findById(item.getId()).orElse(null);
        assertThat(deleted, nullValue());
    }
}