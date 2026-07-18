package ru.practicum.shareit.requestItemTest.controller;

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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.answers.model.Answer;
import ru.practicum.shareit.request.answers.repository.AnswerRepository;
import ru.practicum.shareit.request.controller.RequestController;
import ru.practicum.shareit.request.mapper.RequestItemMapper;
import ru.practicum.shareit.request.model.RequestItem;
import ru.practicum.shareit.request.model.RequestItemRequestDto;
import ru.practicum.shareit.request.model.RequestItemResponseDto;
import ru.practicum.shareit.request.service.RequestItemService;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RequestItemControllerTest {

    @Mock
    private RequestItemService requestItemService;

    @Mock
    private RequestItemMapper mapper;

    @Mock
    private AnswerRepository answerRepository;

    @InjectMocks
    private RequestController controller;

    private ObjectMapper objectMapper;
    private MockMvc mvc;
    private RequestItemResponseDto responseDto;
    private RequestItemRequestDto requestDto;
    private RequestItem requestItem;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        responseDto = RequestItemResponseDto.builder()
                .id(1L)
                .description("Нужна аккумуляторная дрель")
                .requestorId(2L)
                .answers(List.of())
                .build();

        requestDto = RequestItemRequestDto.builder()
                .description("Нужна аккумуляторная дрель")
                .build();

        User user = new User();
        user.setId(2L);

        requestItem = RequestItem.builder()
                .id(1L)
                .description("Нужна аккумуляторная дрель")
                .requestor(user)
                .answers(List.of())
                .build();
    }

    @Test
    void createRequestItem_shouldReturnRequest() throws Exception {
        when(mapper.mapToRequestItem(any(RequestItemRequestDto.class))).thenReturn(requestItem);
        when(requestItemService.createRequestItem(any(RequestItem.class), eq(2L))).thenReturn(requestItem);
        when(mapper.mapToDto(requestItem)).thenReturn(responseDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 2L)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Нужна аккумуляторная дрель")))
                .andExpect(jsonPath("$.requestorId", is(2)));
    }

    @Test
    void getAllRequestItemByOwner_shouldReturnList() throws Exception {
        when(requestItemService.getAllRequestItemByOwner(2L)).thenReturn(List.of(requestItem));
        when(mapper.mapToDto(requestItem)).thenReturn(responseDto);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Нужна аккумуляторная дрель")));
    }

    @Test
    void getAllRequests_shouldReturnList() throws Exception {
        when(requestItemService.getAllRequestItemAllUsers(1L)).thenReturn(List.of(requestItem));
        when(mapper.mapToDto(requestItem)).thenReturn(responseDto);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        when(requestItemService.getRequestItemById(1L)).thenReturn(requestItem);
        when(mapper.mapToDto(requestItem)).thenReturn(responseDto);

        mvc.perform(get("/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Нужна аккумуляторная дрель")));
    }

    @Test
    void getRequestById_shouldEnrichWithAnswers() throws Exception {
        Long requestId = 1L;

        User requestor = new User();
        requestor.setId(1L);

        RequestItem requestItem = new RequestItem();
        requestItem.setId(requestId);
        requestItem.setDescription("Нужна дрель");
        requestItem.setRequestor(requestor);
        requestItem.setAnswers(List.of());

        User owner = new User();
        owner.setId(2L);
        owner.setName("Владелец");

        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setOwner(owner);

        Answer answer = new Answer();
        answer.setId(1L);
        answer.setRequest(requestItem);
        answer.setItem(item);

        RequestItemResponseDto responseDto = RequestItemResponseDto.builder()
                .id(requestId)
                .description("Нужна дрель")
                .requestorId(1L)
                .answers(List.of())
                .build();

        when(requestItemService.getRequestItemById(requestId)).thenReturn(requestItem);
        when(mapper.mapToDto(any(RequestItem.class))).thenReturn(responseDto);

        mvc.perform(get("/requests/{id}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Нужна дрель")));
    }

    @Test
    void enrichWithAnswers_shouldAddAnswersToResponse() throws Exception {
        Long requestId = 1L;

        User requestor = new User();
        requestor.setId(1L);

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setOwner(owner);

        RequestItem requestItem = new RequestItem();
        requestItem.setId(requestId);
        requestItem.setDescription("Нужна аккумуляторная дрель");
        requestItem.setRequestor(requestor);

        Answer answer = new Answer();
        answer.setId(1L);
        answer.setRequest(requestItem);
        answer.setItem(item);

        requestItem.setAnswers(List.of(answer));

        RequestItemResponseDto responseDto = RequestItemResponseDto.builder()
                .id(requestId)
                .description("Нужна аккумуляторная дрель")
                .requestorId(1L)
                .answers(List.of())
                .items(List.of())
                .build();

        when(requestItemService.getRequestItemById(requestId)).thenReturn(requestItem);
        when(mapper.mapToDto(any(RequestItem.class))).thenReturn(responseDto);

        mvc.perform(get("/requests/{id}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Нужна аккумуляторная дрель")))
                .andExpect(jsonPath("$.answers.length()", is(1)))
                .andExpect(jsonPath("$.answers[0].id", is(1)))
                .andExpect(jsonPath("$.answers[0].itemId", is(1)))
                .andExpect(jsonPath("$.answers[0].name", is("Дрель")))
                .andExpect(jsonPath("$.answers[0].ownerId", is(2)))
                .andExpect(jsonPath("$.items.length()", is(1)));
    }

    @Test
    void enrichWithAnswers_shouldHandleRequestWithoutAnswers() throws Exception {
        Long requestId = 2L;

        User requestor = new User();
        requestor.setId(1L);

        RequestItem requestWithoutAnswers = new RequestItem();
        requestWithoutAnswers.setId(requestId);
        requestWithoutAnswers.setDescription("Запрос без ответов");
        requestWithoutAnswers.setRequestor(requestor);
        requestWithoutAnswers.setAnswers(List.of());

        RequestItemResponseDto dtoWithoutAnswers = RequestItemResponseDto.builder()
                .id(requestId)
                .description("Запрос без ответов")
                .answers(List.of())
                .items(List.of())
                .build();

        when(requestItemService.getRequestItemById(requestId)).thenReturn(requestWithoutAnswers);
        when(mapper.mapToDto(requestWithoutAnswers)).thenReturn(dtoWithoutAnswers);

        mvc.perform(get("/requests/{id}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.answers.length()", is(0)))
                .andExpect(jsonPath("$.items.length()", is(0)));
    }

    @Test
    void enrichWithAnswers_shouldHandleMultipleAnswers() throws Exception {
        Long requestId = 1L;

        User requestor = new User();
        requestor.setId(1L);

        User owner1 = new User();
        owner1.setId(2L);

        User owner2 = new User();
        owner2.setId(3L);

        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Дрель");
        item1.setOwner(owner1);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Перфоратор");
        item2.setOwner(owner2);

        RequestItem requestItem = new RequestItem();
        requestItem.setId(requestId);
        requestItem.setDescription("Нужна аккумуляторная дрель");
        requestItem.setRequestor(requestor);

        Answer answer1 = new Answer();
        answer1.setId(1L);
        answer1.setRequest(requestItem);
        answer1.setItem(item1);

        Answer answer2 = new Answer();
        answer2.setId(2L);
        answer2.setRequest(requestItem);
        answer2.setItem(item2);

        requestItem.setAnswers(List.of(answer1, answer2));

        RequestItemResponseDto responseDto = RequestItemResponseDto.builder()
                .id(requestId)
                .description("Нужна аккумуляторная дрель")
                .requestorId(1L)
                .answers(List.of())
                .items(List.of())
                .build();

        when(requestItemService.getRequestItemById(requestId)).thenReturn(requestItem);
        when(mapper.mapToDto(requestItem)).thenReturn(responseDto);

        mvc.perform(get("/requests/{id}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answers.length()", is(2)))
                .andExpect(jsonPath("$.answers[0].id", is(1)))
                .andExpect(jsonPath("$.answers[1].id", is(2)))
                .andExpect(jsonPath("$.items.length()", is(2)));
    }
}