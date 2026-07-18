package ru.practicum.shareit.itemTest.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comments.mapper.CommentMapper;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.comments.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemWithDetails;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.answers.model.Answer;
import ru.practicum.shareit.request.answers.repository.AnswerRepository;
import ru.practicum.shareit.request.model.RequestItem;
import ru.practicum.shareit.request.repository.RequestItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceUnitTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private RequestItemRepository requestItemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Test
    void createItem_shouldSaveAndReturnItem() {
        User owner = new User();
        owner.setId(1L);

        Item itemToSave = new Item();
        itemToSave.setName("Дрель");
        itemToSave.setDescription("Аккумуляторная");
        itemToSave.setAvailable(true);

        Item savedItem = new Item();
        savedItem.setId(1L);
        savedItem.setName("Дрель");
        savedItem.setDescription("Аккумуляторная");
        savedItem.setAvailable(true);
        savedItem.setOwner(owner);

        when(userService.getUserById(1L)).thenReturn(owner);
        when(itemRepository.save(itemToSave)).thenReturn(savedItem);

        Item result = itemService.createItem(itemToSave, 1L, null);

        assertThat(result.getId(), is(1L));
        assertThat(result.getName(), is("Дрель"));
        assertThat(result.getAvailable(), is(true));
        verify(itemRepository, times(1)).save(itemToSave);
    }

    @Test
    void updateItem_shouldUpdate_whenOwnerMatches() {
        Item newItemData = new Item();
        newItemData.setName("Новое имя");

        User owner = new User();
        owner.setId(1L);

        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setName("Старое имя");
        existingItem.setOwner(owner);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(existingItem)).thenReturn(existingItem);

        Item result = itemService.updateItem(newItemData, 1L, 1L);

        assertThat(result.getName(), is("Новое имя"));
    }

    @Test
    void updateItem_shouldThrowAccessDenied_whenOwnerNotMatches() {
        User owner = new User();
        owner.setId(1L);

        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setOwner(owner);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));

        assertThatThrownBy(() -> itemService.updateItem(new Item(), 1L, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Редактировать можно только свои вещи");
    }

    @Test
    void findItemById_shouldThrowNotFound_whenItemNotExists() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findItemById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    void deleteItem_shouldDelete_whenOwnerMatches() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(1L);
        item.setOwner(owner);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        doNothing().when(itemRepository).deleteById(1L);

        itemService.deleteItem(1L, 1L);

        verify(itemRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteItem_shouldThrowAccessDenied_whenOwnerNotMatches() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(1L);
        item.setOwner(owner);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.deleteItem(1L, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Удалять можно только свои вещи");
    }

    @Test
    void updateItem_shouldUpdateOnlyName() {
        User owner = new User();
        owner.setId(1L);

        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setName("Старое имя");
        existingItem.setDescription("Старое описание");
        existingItem.setAvailable(true);
        existingItem.setOwner(owner);

        Item updateData = new Item();
        updateData.setName("Новое имя");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(existingItem)).thenReturn(existingItem);

        Item result = itemService.updateItem(updateData, 1L, 1L);

        assertEquals("Новое имя", result.getName());
        assertEquals("Старое описание", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void updateItem_shouldUpdateOnlyDescription() {
        User owner = new User();
        owner.setId(1L);

        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setName("Старое имя");
        existingItem.setDescription("Старое описание");
        existingItem.setAvailable(true);
        existingItem.setOwner(owner);

        Item updateData = new Item();
        updateData.setDescription("Новое описание");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(existingItem)).thenReturn(existingItem);

        Item result = itemService.updateItem(updateData, 1L, 1L);

        assertEquals("Старое имя", result.getName());
        assertEquals("Новое описание", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void updateItem_shouldUpdateOnlyAvailable() {
        User owner = new User();
        owner.setId(1L);

        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setName("Старое имя");
        existingItem.setDescription("Старое описание");
        existingItem.setAvailable(true);
        existingItem.setOwner(owner);

        Item updateData = new Item();
        updateData.setAvailable(false);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(existingItem)).thenReturn(existingItem);

        Item result = itemService.updateItem(updateData, 1L, 1L);

        assertEquals("Старое имя", result.getName());
        assertEquals("Старое описание", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void searchItems_shouldReturnEmpty_whenTextIsEmpty() {
        when(itemRepository.search("")).thenReturn(List.of());

        List<Item> result = itemService.searchItems("");

        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_shouldReturnEmpty_whenTextIsNull() {
        when(itemRepository.search(null)).thenReturn(List.of());

        List<Item> result = itemService.searchItems(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteItem_shouldThrowNotFound_whenItemNotExists() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.deleteItem(999L, 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findAllItemsByUser_shouldReturnEmpty_whenUserHasNoItems() {
        when(itemRepository.findAllByOwnerId(1L)).thenReturn(List.of());

        List<Item> result = itemService.findAllItemsByUser(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void addComment_shouldSaveComment() {
        Long itemId = 1L;
        Long userId = 2L;
        String text = "Отличная вещь!";

        Item item = new Item();
        item.setId(itemId);

        User author = new User();
        author.setId(userId);

        Booking pastBooking = new Booking();
        pastBooking.setId(1L);

        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setText(text);
        savedComment.setItem(item);
        savedComment.setAuthor(author);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userService.getUserById(userId)).thenReturn(author);
        when(bookingRepository.findFirstByItemIdAndBookerIdAndEndBefore(eq(itemId), eq(userId), any()))
                .thenReturn(Optional.of(pastBooking));
        when(commentMapper.toComment(eq(text), eq(item), eq(author))).thenReturn(savedComment);
        when(commentRepository.save(any())).thenReturn(savedComment);

        Comment result = itemService.addComment(itemId, userId, text);

        assertThat(result.getId(), is(1L));
        assertThat(result.getText(), is(text));
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void createItem_shouldSaveWithRequestId() {
        User owner = new User();
        owner.setId(1L);

        RequestItem request = new RequestItem();
        request.setId(1L);

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Аккумуляторная");
        item.setAvailable(true);

        Item savedItem = new Item();
        savedItem.setId(1L);
        savedItem.setName("Дрель");
        savedItem.setRequest(request);
        savedItem.setOwner(owner);

        when(userService.getUserById(1L)).thenReturn(owner);
        when(requestItemRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.save(any())).thenReturn(savedItem);
        when(answerRepository.save(any())).thenReturn(new Answer());

        Item result = itemService.createItem(item, 1L, 1L);

        assertThat(result.getId(), is(1L));
        assertThat(result.getName(), is("Дрель"));
        verify(requestItemRepository, times(1)).findById(1L);
        verify(answerRepository, times(1)).save(any());
    }

    @Test
    void createItem_shouldThrow_whenRequestNotFound() {
        User owner = new User();
        owner.setId(1L);

        RequestItem request = new RequestItem();
        request.setId(999L);

        Item item = new Item();
        item.setName("Дрель");

        when(userService.getUserById(1L)).thenReturn(owner);
        when(requestItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(item, 1L, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Запрос вещи не найден");
    }

    @Test
    void addComment_shouldThrowNotFound_whenItemNotFound() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(999L, 1L, "text"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    void addComment_shouldThrow_whenUserNotBookedItem() {
        Long itemId = 1L;
        Long userId = 2L;

        Item item = new Item();
        item.setId(itemId);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userService.getUserById(userId)).thenReturn(new User());
        when(bookingRepository.findFirstByItemIdAndBookerIdAndEndBefore(eq(itemId), eq(userId), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(itemId, userId, "text"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Пользователь не брал эту вещь в аренду");
    }

    @Test
    void createItem_shouldSetRequest_whenRequestExists() {
        User owner = new User();
        owner.setId(1L);

        RequestItem request = new RequestItem();
        request.setId(1L);

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Аккумуляторная");
        item.setAvailable(true);

        Item savedItem = new Item();
        savedItem.setId(1L);
        savedItem.setName("Дрель");
        savedItem.setRequest(request);
        savedItem.setOwner(owner);

        when(userService.getUserById(1L)).thenReturn(owner);
        when(requestItemRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.save(any())).thenReturn(savedItem);
        when(answerRepository.save(any())).thenReturn(new Answer());

        Item result = itemService.createItem(item, 1L, 1L);

        assertThat(result.getId(), is(1L));
        assertThat(result.getName(), is("Дрель"));
        assertThat(result.getRequest(), is(request));

        verify(requestItemRepository, times(1)).findById(1L);
        verify(answerRepository, times(1)).save(any());
    }

    @Test
    void createItem_shouldNotSetRequest_whenRequestIsNull() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Аккумуляторная");
        item.setAvailable(true);
        // item.setRequest(null);   // ← УБРАТЬ!

        Item savedItem = new Item();
        savedItem.setId(1L);
        savedItem.setName("Дрель");
        savedItem.setOwner(owner);

        when(userService.getUserById(1L)).thenReturn(owner);
        when(itemRepository.save(any())).thenReturn(savedItem);

        Item result = itemService.createItem(item, 1L, null);  // ← добавили null

        assertThat(result.getId(), is(1L));
        assertThat(result.getName(), is("Дрель"));

        verify(requestItemRepository, never()).findById(any());
        verify(answerRepository, never()).save(any());
    }

    @Test
    void findAllItemsByUserWithDetails_shouldReturnItemsWithBookingsAndComments() {
        Long ownerId = 1L;
        LocalDateTime now = LocalDateTime.now();

        User owner = new User();
        owner.setId(ownerId);

        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Дрель");
        item1.setOwner(owner);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Молоток");
        item2.setOwner(owner);

        List<Item> items = List.of(item1, item2);

        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setItem(item1);

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setItem(item1);

        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setItem(item1);

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setItem(item2);

        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(items);
        when(bookingRepository.findAllByItemIdIn(anyList())).thenReturn(List.of(booking1, booking2));
        when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of(comment1, comment2));

        List<ItemWithDetails> result = itemService.findAllItemsByUserWithDetails(ownerId);

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getItem().getId(), is(1L));
        assertThat(result.get(0).getBookings(), hasSize(2));
        assertThat(result.get(0).getComments(), hasSize(1));

        verify(itemRepository).findAllByOwnerId(ownerId);
        verify(bookingRepository).findAllByItemIdIn(anyList());
        verify(commentRepository).findAllByItemIdIn(anyList());
    }

    @Test
    void findAllItemsByUserWithDetails_shouldReturnEmptyList_whenNoItems() {
        Long ownerId = 999L;

        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(List.of());

        List<ItemWithDetails> result = itemService.findAllItemsByUserWithDetails(ownerId);

        assertTrue(result.isEmpty());
        verify(bookingRepository, never()).findAllByItemIdIn(any());
        verify(commentRepository, never()).findAllByItemIdIn(any());
    }

    @Test
    void findAllItemsByUserWithDetails_shouldHandleItemsWithoutDetails() {
        Long ownerId = 1L;

        Item itemWithoutDetails = new Item();
        itemWithoutDetails.setId(3L);
        itemWithoutDetails.setName("Без деталей");

        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(List.of(itemWithoutDetails));
        when(bookingRepository.findAllByItemIdIn(anyList())).thenReturn(List.of());
        when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of());

        List<ItemWithDetails> result = itemService.findAllItemsByUserWithDetails(ownerId);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getBookings(), empty());
        assertThat(result.get(0).getComments(), empty());
    }

}