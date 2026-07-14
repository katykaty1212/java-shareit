package ru.practicum.shareit.itemTest.controller;

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
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.comments.mapper.CommentMapper;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.comments.model.CommentDto;
import ru.practicum.shareit.item.comments.repository.CommentRepository;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.mapper.ItemMapperMapstruct;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemRequestDto;
import ru.practicum.shareit.item.model.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @Mock
    private ItemMapperMapstruct itemMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemController controller;

    private ObjectMapper mapper;
    private MockMvc mvc;
    private ItemResponseDto responseDto;
    private ItemRequestDto requestDto;
    private CommentDto commentDto;
    private Item item;
    private Comment comment;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        responseDto = new ItemResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Дрель");
        responseDto.setDescription("Аккумуляторная");
        responseDto.setAvailable(true);
        responseDto.setOwnerId(1L);
        responseDto.setComments(List.of());

        requestDto = new ItemRequestDto();
        requestDto.setName("Дрель");
        requestDto.setDescription("Аккумуляторная");
        requestDto.setAvailable(true);

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Отличная вещь!");
        commentDto.setAuthorName("Иван");

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Отличная вещь!");
    }

    @Test
    void createItem_shouldReturnItem() throws Exception {
        when(itemMapper.mapToItem(any(ItemRequestDto.class), isNull())).thenReturn(item);
        when(itemService.createItem(any(Item.class), eq(1L), isNull())).thenReturn(item);  // ← добавили isNull()
        when(itemMapper.mapToDto(item)).thenReturn(responseDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Дрель")));
    }

    @Test
    void getItemById_shouldReturnItem() throws Exception {
        when(itemService.findItemById(1L)).thenReturn(item);
        when(itemMapper.mapToDto(item)).thenReturn(responseDto);

        mvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Дрель")));
    }

    @Test
    void deleteItem_shouldReturnOk() throws Exception {
        mvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_shouldReturnComment() throws Exception {
        when(itemService.addComment(eq(1L), eq(2L), eq("Отличная вещь!"))).thenReturn(comment);
        when(commentMapper.toDto(comment)).thenReturn(commentDto);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .content("Отличная вещь!")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Отличная вещь!")));
    }

    @Test
    void findAllItemsByUser_shouldCallEnrich() throws Exception {
        Long ownerId = 1L;

        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");

        when(itemService.findAllItemsByUser(ownerId)).thenReturn(List.of(item));
        when(itemMapper.mapToDto(any(Item.class))).thenReturn(new ItemResponseDto());
        when(bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(anyLong(), any()))
                .thenReturn(Optional.empty());
        when(bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(anyLong(), any()))
                .thenReturn(Optional.empty());
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of());

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk());

        verify(itemService, times(1)).findAllItemsByUser(ownerId);
    }

    @Test
    void findItemById_shouldCallEnrich() throws Exception {
        Long itemId = 1L;

        Item item = new Item();
        item.setId(itemId);
        item.setName("Дрель");

        when(itemService.findItemById(itemId)).thenReturn(item);
        when(itemMapper.mapToDto(any(Item.class))).thenReturn(new ItemResponseDto());
        when(bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(anyLong(), any()))
                .thenReturn(Optional.empty());
        when(bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(anyLong(), any()))
                .thenReturn(Optional.empty());
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of());

        mvc.perform(get("/items/" + itemId))
                .andExpect(status().isOk());

        verify(itemService, times(1)).findItemById(itemId);
    }

    @Test
    void addComment_shouldCallCommentMapper() throws Exception {
        Long itemId = 1L;
        Long userId = 2L;
        String text = "Отличная вещь!";

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText(text);

        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText(text);

        when(itemService.addComment(eq(itemId), eq(userId), eq(text))).thenReturn(comment);
        when(commentMapper.toDto(comment)).thenReturn(commentDto);

        mvc.perform(post("/items/" + itemId + "/comment")
                        .header("X-Sharer-User-Id", userId)
                        .content(text)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(commentMapper, times(1)).toDto(comment);
    }

    @Test
    void findItemById_shouldEnrichWithBookingsAndComments() throws Exception {
        Long itemId = 1L;

        Item item = new Item();
        item.setId(itemId);
        item.setName("Дрель");

        when(itemService.findItemById(itemId)).thenReturn(item);
        when(itemMapper.mapToDto(any(Item.class))).thenReturn(new ItemResponseDto());
        when(bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(anyLong(), any()))
                .thenReturn(Optional.empty());
        when(bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(anyLong(), any()))
                .thenReturn(Optional.empty());
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of());

        mvc.perform(get("/items/" + itemId))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_shouldReturnList() throws Exception {
        String text = "дрель";

        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Аккумуляторная");
        item.setAvailable(true);

        ItemResponseDto responseDto = new ItemResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Дрель");
        responseDto.setDescription("Аккумуляторная");
        responseDto.setAvailable(true);

        when(itemService.searchItems(text)).thenReturn(List.of(item));
        when(itemMapper.mapToDto(any(Item.class))).thenReturn(responseDto);

        mvc.perform(get("/items/search")
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Дрель")));
    }

    @Test
    void searchItems_shouldReturnEmptyList_whenTextNotFound() throws Exception {
        String text = "несуществующая";

        when(itemService.searchItems(text)).thenReturn(List.of());

        mvc.perform(get("/items/search")
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        Long itemId = 1L;
        Long ownerId = 1L;

        ItemRequestDto dto = new ItemRequestDto();
        dto.setName("Новое имя");
        dto.setDescription("Новое описание");
        dto.setAvailable(false);

        Item item = new Item();
        item.setId(itemId);
        item.setName("Новое имя");
        item.setDescription("Новое описание");
        item.setAvailable(false);

        ItemResponseDto responseDto = new ItemResponseDto();
        responseDto.setId(itemId);
        responseDto.setName("Новое имя");
        responseDto.setDescription("Новое описание");
        responseDto.setAvailable(false);

        when(itemMapper.mapToItem(any(ItemRequestDto.class), isNull())).thenReturn(item);
        when(itemService.updateItem(any(Item.class), eq(itemId), eq(ownerId))).thenReturn(item);
        when(itemMapper.mapToDto(any(Item.class))).thenReturn(responseDto);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.name", is("Новое имя")))
                .andExpect(jsonPath("$.available", is(false)));
    }
}