package ru.practicum.shareit.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlerTest {

    private ErrorHandler errorHandler;

    @BeforeEach
    void setUp() {
        errorHandler = new ErrorHandler();
    }

    @Test
    void handleNotFound_shouldReturn404WithMessage() {
        String errorMessage = "Пользователь с id 1 не найден";

        NotFoundException exception = new NotFoundException(errorMessage);

        ErrorResponse response = errorHandler.handleNotFound(exception);

        assertNotNull(response);
        assertEquals(errorMessage, response.getError());
    }

    @Test
    void handleAccessDenied_shouldReturn403WithMessage() {
        String errorMessage = "Доступ запрещен";
        AccessDeniedException exception = new AccessDeniedException(errorMessage);

        ErrorResponse response = errorHandler.handleAccessDenied(exception);

        assertNotNull(response);
        assertEquals(errorMessage, response.getError());
    }

    @Test
    void handleNotFound_shouldReturnCorrectErrorResponse() {
        NotFoundException exception = new NotFoundException("Вещь не найдена");

        ErrorResponse response = errorHandler.handleNotFound(exception);

        assertNotNull(response);
        assertEquals("Вещь не найдена", response.getError());
    }

    @Test
    void handleAccessDenied_shouldReturnCorrectErrorResponse() {
        // given
        AccessDeniedException exception = new AccessDeniedException("Нет прав");

        // when
        ErrorResponse response = errorHandler.handleAccessDenied(exception);

        // then
        assertNotNull(response);
        assertEquals("Нет прав", response.getError());
    }
}