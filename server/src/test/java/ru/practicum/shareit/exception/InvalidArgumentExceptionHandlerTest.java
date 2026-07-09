package ru.practicum.shareit.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;

class InvalidArgumentExceptionHandlerTest {

    private InvalidArgumentExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new InvalidArgumentExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        webRequest = new ServletWebRequest(request);
    }

    @Test
    void handleIllegalArgumentException_shouldReturn400WithMessage() {
        String errorMessage = "Дата начала должна быть раньше даты окончания";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        ResponseEntity<Object> response = handler.handleConflict(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void handleIllegalArgumentException_shouldReturn400WithCustomMessage() {
        String errorMessage = "Вещь недоступна для бронирования";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        ResponseEntity<Object> response = handler.handleConflict(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void handleIllegalArgumentException_shouldReturn400WithEmptyMessage() {
        IllegalArgumentException exception = new IllegalArgumentException("");

        ResponseEntity<Object> response = handler.handleConflict(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("", response.getBody());
    }

    @Test
    void handleIllegalArgumentException_shouldReturn400WithNullMessage() {
        IllegalArgumentException exception = new IllegalArgumentException((String) null);

        ResponseEntity<Object> response = handler.handleConflict(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }
}