package ru.practicum.shareit.requestItemTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.answers.repository.AnswerRepository;
import ru.practicum.shareit.request.model.RequestItem;
import ru.practicum.shareit.request.repository.RequestItemRepository;
import ru.practicum.shareit.request.service.RequestItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestItemTest {

    @Mock
    private RequestItemRepository requestItemRepository;

    @Mock
    private UserService userService;

    @Mock
    private AnswerRepository answerRepository;

    @InjectMocks
    private RequestItemServiceImpl requestItemService;

    @Test
    void createRequestItemTest() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@u.com");

        RequestItem item = RequestItem.builder()
                .description("Запрос дрели.")
                .build();

        RequestItem savedItem = RequestItem.builder()
                .id(1L)
                .description("Запрос дрели.")
                .requestor(user)
                .build();

        when(userService.getUserById(1L)).thenReturn(user);
        when(requestItemRepository.save(any(RequestItem.class))).thenReturn(savedItem);

        RequestItem result = requestItemService.createRequestItem(item, 1L);

        assertThat(result.getDescription(), is("Запрос дрели."));
        assertThat(result.getRequestor().getId(), is(1L));

        verify(requestItemRepository, times(1)).save(any(RequestItem.class));
    }

    @Test
    void getAllRequestItemAllUsersTest() {
        User currentUser = new User();
        currentUser.setId(1L);

        User otherUser = new User();
        otherUser.setId(2L);

        RequestItem item1 = RequestItem.builder()
                .id(1L)
                .description("Запрос дрели.")
                .requestor(otherUser)
                .build();

        RequestItem item2 = RequestItem.builder()
                .id(2L)
                .description("Запрос молотка.")
                .requestor(otherUser)
                .build();

        when(requestItemRepository.findAllByRequestorIdNotOrderByCreatedDesc(currentUser.getId()))
                .thenReturn(List.of(item1, item2));
        when(answerRepository.findByRequestIdIn(anyList())).thenReturn(List.of());

        List<RequestItem> result = requestItemService.getAllRequestItemAllUsers(currentUser.getId());

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getDescription(), is("Запрос дрели."));
        assertThat(result.get(1).getDescription(), is("Запрос молотка."));

        verify(requestItemRepository, times(1)).
                findAllByRequestorIdNotOrderByCreatedDesc(currentUser.getId());
    }

    @Test
    void getRequestItemById_shouldReturnRequest() {
        User user = new User();
        user.setId(1L);

        RequestItem request = RequestItem.builder()
                .id(1L)
                .description("Нужна дрель")
                .requestor(user)
                .build();

        when(requestItemRepository.findById(1L)).thenReturn(Optional.of(request));

        RequestItem result = requestItemService.getRequestItemById(1L);

        assertThat(result.getId(), is(1L));
        assertThat(result.getDescription(), is("Нужна дрель"));
        assertThat(result.getRequestor().getId(), is(1L));
    }

    @Test
    void getRequestItemById_shouldThrowNotFound_whenRequestNotExists() {
        when(requestItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestItemService.getRequestItemById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Запрос не найден");
    }

    @Test
    void getMyRequests_shouldReturnUserRequests() {
        User user = new User();
        user.setId(1L);

        RequestItem request1 = RequestItem.builder()
                .id(1L)
                .description("Запрос 1")
                .requestor(user)
                .build();

        RequestItem request2 = RequestItem.builder()
                .id(2L)
                .description("Запрос 2")
                .requestor(user)
                .build();

        when(requestItemRepository.findAllByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(request1, request2));

        when(answerRepository.findByRequestIdIn(anyList())).thenReturn(List.of());

        List<RequestItem> result = requestItemService.getAllRequestItemByOwner(1L);

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getDescription(), is("Запрос 1"));
        assertThat(result.get(1).getDescription(), is("Запрос 2"));
    }
}