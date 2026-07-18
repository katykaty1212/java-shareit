package ru.practicum.shareit.comments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.comments.mapper.CommentMapper;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.comments.model.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    private CommentMapper mapper;
    private Comment comment;
    private User author;
    private Item item;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CommentMapper.class);

        author = new User();
        author.setId(1L);
        author.setName("Иван Петров");

        item = new Item();
        item.setId(10L);
        item.setName("Дрель");

        comment = new Comment();
        comment.setId(100L);
        comment.setText("Отличная вещь, всё работает!");
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(Instant.parse("2025-12-20T10:30:00Z"));
    }

    @Test
    void toDto_shouldMapCommentToDto() {
        CommentDto dto = mapper.toDto(comment);

        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals("Отличная вещь, всё работает!", dto.getText());
        assertEquals("Иван Петров", dto.getAuthorName());
        assertNotNull(dto.getCreated());
    }

    @Test
    void toDto_shouldFormatDateCorrectly() {
        CommentDto dto = mapper.toDto(comment);

        String created = dto.getCreated();
        assertNotNull(created);

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy.MM.dd, hh:mm:ss")
                .withZone(ZoneId.of("UTC"));
        String expected = formatter.format(comment.getCreated());

        assertEquals(expected, created);
    }

    @Test
    void toDto_shouldHandleNullComment() {
        CommentDto dto = mapper.toDto(null);
        assertNull(dto);
    }

    @Test
    void toDto_shouldHandleNullAuthor() {
        comment.setAuthor(null);
        CommentDto dto = mapper.toDto(comment);

        assertNotNull(dto);
        assertNull(dto.getAuthorName());
        assertEquals(100L, dto.getId());
        assertEquals("Отличная вещь, всё работает!", dto.getText());
    }

    @Test
    void toComment_shouldCreateCommentFromTextItemAndAuthor() {
        String text = "Отличная вещь!";
        Item item = new Item();
        item.setId(1L);
        User author = new User();
        author.setId(1L);

        Comment comment = mapper.toComment(text, item, author);

        assertNotNull(comment);
        assertNull(comment.getId());
        assertEquals(text, comment.getText());
        assertEquals(item, comment.getItem());
        assertEquals(author, comment.getAuthor());
        assertNotNull(comment.getCreated());
    }

    @Test
    void toComment_shouldHandleNullText() {
        Item item = new Item();
        item.setId(1L);
        User author = new User();
        author.setId(1L);

        Comment comment = mapper.toComment(null, item, author);

        assertNotNull(comment);
        assertNull(comment.getText());
        assertEquals(item, comment.getItem());
        assertEquals(author, comment.getAuthor());
    }

    @Test
    void toComment_shouldHandleNullItem() {
        String text = "Отличная вещь!";
        User author = new User();
        author.setId(1L);

        Comment comment = mapper.toComment(text, null, author);

        assertNotNull(comment);
        assertEquals(text, comment.getText());
        assertNull(comment.getItem());
        assertEquals(author, comment.getAuthor());
    }

    @Test
    void toComment_shouldHandleNullAuthor() {
        String text = "Отличная вещь!";
        Item item = new Item();
        item.setId(1L);

        Comment comment = mapper.toComment(text, item, null);

        assertNotNull(comment);
        assertEquals(text, comment.getText());
        assertEquals(item, comment.getItem());
        assertNull(comment.getAuthor());
    }
}