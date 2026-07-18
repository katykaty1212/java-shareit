package ru.practicum.shareit.itemTest.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
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
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;


    @Autowired
    private EntityManager em;

    private User owner;
    private Item testItem;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .name("Тестовый пользователь")
                .email("test@mail.com")
                .state(UserState.ACTIVE)
                .build();
        em.persist(owner);

        testItem = new Item();
        testItem.setName("Тестовая дрель");
        testItem.setDescription("Тестовое описание");
        testItem.setAvailable(true);
        testItem.setOwner(owner);
        em.persist(testItem);

        em.flush();
    }

    @Test
    void createItem_shouldSaveAndReturnItem() {
        Item newItem = new Item();
        newItem.setName("Дрель");
        newItem.setDescription("Аккумуляторная дрель");
        newItem.setAvailable(true);

        Item result = itemService.createItem(newItem, owner.getId(), null);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), is("Дрель"));
        assertThat(result.getDescription(), is("Аккумуляторная дрель"));
        assertThat(result.getAvailable(), is(true));
        assertThat(result.getOwner().getId(), is(owner.getId()));

        Item savedItem = em.find(Item.class, result.getId());
        assertThat(savedItem, notNullValue());
        assertThat(savedItem.getName(), is("Дрель"));
    }

    @Test
    void updateItem_shouldUpdateExistingItem() {
        Item updateData = new Item();
        updateData.setName("Обновленная дрель");
        updateData.setDescription("Обновленное описание");
        updateData.setAvailable(false);

        Item result = itemService.updateItem(updateData, testItem.getId(), owner.getId());

        assertThat(result.getName(), is("Обновленная дрель"));
        assertThat(result.getDescription(), is("Обновленное описание"));
        assertThat(result.getAvailable(), is(false));

        em.flush();
        em.clear();
        Item updatedItem = em.find(Item.class, testItem.getId());
        assertThat(updatedItem.getName(), is("Обновленная дрель"));
    }

    @Test
    void findItemById_shouldReturnItem() {
        Item result = itemService.findItemById(testItem.getId());

        assertThat(result.getId(), is(testItem.getId()));
        assertThat(result.getName(), is("Тестовая дрель"));
        assertThat(result.getDescription(), is("Тестовое описание"));
        assertThat(result.getAvailable(), is(true));
    }

    @Test
    void findAllItemsByUser_shouldReturnUserItems() {
        Item secondItem = new Item();
        secondItem.setName("Второй предмет");
        secondItem.setDescription("Описание второго предмета");
        secondItem.setAvailable(false);
        secondItem.setOwner(owner);
        em.persist(secondItem);
        em.flush();

        List<Item> result = itemService.findAllItemsByUser(owner.getId());

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getOwner().getId(), is(owner.getId()));
        assertThat(result.get(1).getOwner().getId(), is(owner.getId()));
    }

    @Test
    void searchItems_shouldReturnOnlyAvailableItems() {
        Item notAvailableItem = new Item();
        notAvailableItem.setName("Дрель сетевая");
        notAvailableItem.setDescription("Старая сетевая дрель");
        notAvailableItem.setAvailable(false);
        notAvailableItem.setOwner(owner);
        em.persist(notAvailableItem);

        testItem.setName("Аккумуляторная дрель");
        testItem.setAvailable(true);
        em.merge(testItem);
        em.flush();

        List<Item> result = itemService.searchItems("дрель");

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getAvailable(), is(true));
        assertThat(result.get(0).getName(), containsStringIgnoringCase("дрель"));
    }

    @Test
    void deleteItem_shouldRemoveItem() {
        itemService.deleteItem(testItem.getId(), owner.getId());

        em.flush();
        em.clear();
        Item deletedItem = em.find(Item.class, testItem.getId());
        assertThat(deletedItem, nullValue());
    }
}
