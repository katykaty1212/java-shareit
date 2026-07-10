package ru.practicum.shareit.requestItemTest.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.answers.model.Answer;
import ru.practicum.shareit.request.mapper.RequestItemMapper;
import ru.practicum.shareit.request.model.RequestItem;
import ru.practicum.shareit.request.model.RequestItemRequestDto;
import ru.practicum.shareit.request.model.RequestItemResponseDto;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestItemMapperTest {

    private RequestItemMapper mapper;
    private User requestor;
    private RequestItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(RequestItemMapper.class);

        requestor = new User();
        requestor.setId(1L);
        requestor.setName("Иван");
        requestor.setEmail("ivan@mail.com");

        requestDto = RequestItemRequestDto.builder()
                .description("Нужна аккумуляторная дрель")
                .build();
    }

    @Test
    void mapToRequestItem_shouldMapDtoAndRequestorIdToRequestItem() {
        RequestItem requestItem = mapper.mapToRequestItem(requestDto);

        assertNotNull(requestItem);
        assertNull(requestItem.getId());
        assertEquals("Нужна аккумуляторная дрель", requestItem.getDescription());
        assertNull(requestItem.getRequestor());
        assertNull(requestItem.getCreated());
    }

    @Test
    void mapToDto_shouldMapRequestItemToResponseDto() {
        RequestItem requestItem = RequestItem.builder()
                .id(10L)
                .description("Нужна аккумуляторная дрель")
                .requestor(requestor)
                .created(Instant.parse("2025-12-20T10:30:00Z"))
                .build();

        RequestItemResponseDto dto = mapper.mapToDto(requestItem);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Нужна аккумуляторная дрель", dto.getDescription());
        assertEquals(1L, dto.getRequestorId());
        assertNotNull(dto.getCreated());
    }

    @Test
    void mapToDto_shouldFormatDateCorrectly() {
        RequestItem requestItem = RequestItem.builder()
                .id(10L)
                .description("Нужна аккумуляторная дрель")
                .requestor(requestor)
                .created(Instant.parse("2025-12-20T10:30:00Z"))
                .build();

        RequestItemResponseDto dto = mapper.mapToDto(requestItem);

        String created = dto.getCreated();
        assertNotNull(created);

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy.MM.dd, hh:mm:ss")
                .withZone(ZoneId.of("UTC"));
        String expected = formatter.format(requestItem.getCreated());

        assertEquals(expected, created);
    }

    @Test
    void mapToDto_shouldHandleNullRequestItem() {
        RequestItemResponseDto dto = mapper.mapToDto(null);
        assertNull(dto);
    }

    @Test
    void mapToDto_shouldHandleNullRequestor() {
        RequestItem requestItem = RequestItem.builder()
                .id(10L)
                .description("Нужна аккумуляторная дрель")
                .requestor(null)
                .created(Instant.now())
                .build();

        RequestItemResponseDto dto = mapper.mapToDto(requestItem);

        assertNotNull(dto);
        assertNull(dto.getRequestorId());
        assertEquals(10L, dto.getId());
        assertEquals("Нужна аккумуляторная дрель", dto.getDescription());
    }
    @Test
    void mapToDto_shouldMapAnswerFields() {
        User requestor = new User();
        requestor.setId(1L);

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setOwner(owner);

        RequestItem request = new RequestItem();
        request.setId(1L);
        request.setDescription("Нужна дрель");
        request.setRequestor(requestor);

        Answer answer = new Answer();
        answer.setId(1L);
        answer.setRequest(request);
        answer.setItem(item);

        RequestItemResponseDto dto = mapper.mapToDto(request);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Нужна дрель", dto.getDescription());
        assertEquals(1L, dto.getRequestorId());
    }

    @Test
    void mapToDto_shouldMapAnswerList() {
        User requestor = new User();
        requestor.setId(1L);

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setOwner(owner);

        RequestItem request = new RequestItem();
        request.setId(1L);
        request.setDescription("Нужна дрель");
        request.setRequestor(requestor);

        Answer answer1 = new Answer();
        answer1.setId(1L);
        answer1.setRequest(request);
        answer1.setItem(item);

        Answer answer2 = new Answer();
        answer2.setId(2L);
        answer2.setRequest(request);
        answer2.setItem(item);

        request.setAnswers(List.of(answer1, answer2));

        RequestItemResponseDto dto = mapper.mapToDto(request);

        assertNotNull(dto);
        assertEquals(2, dto.getAnswers().size());
        assertEquals(1L, dto.getAnswers().get(0).getId());
        assertEquals(2L, dto.getAnswers().get(1).getId());
    }
}