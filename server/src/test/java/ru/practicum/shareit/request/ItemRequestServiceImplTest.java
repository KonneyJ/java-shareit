package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        properties = "jdbc.url=jdbc:postgresql://localhost:5432/test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplTest {
    private final EntityManager em;
    private final ItemRequestService service;
    private final UserService userService;

    private ItemRequestDto requestDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setName("Julie");
        userDto.setEmail("julie17@yandex.ru");

        requestDto = new ItemRequestDto();
        requestDto.setId(1);
        requestDto.setDescription("description");
        requestDto.setCreated(LocalDateTime.now());
        requestDto.setItems(List.of());
    }

    @Test
    void createRequestTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();

        service.createRequest(userId, requestDto);

        TypedQuery<ItemRequest> query = em.createQuery("Select r from ItemRequest r where r.description = :description",
                ItemRequest.class);
        ItemRequest request = query.setParameter("description", requestDto.getDescription())
                .getSingleResult();

        assertThat(request.getId(), notNullValue());
        assertThat(request.getDescription(), equalTo(requestDto.getDescription()));
    }

    @Test
    void createRequestByUserNotExistTest() {
        int userId = userDto.getId();

        assertThrows(UserNotFoundException.class, () -> service.createRequest(userId, requestDto));
    }

    @Test
    void getAllRequestsByUserTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        service.createRequest(userId, requestDto);
        List<ItemRequestDto> sourceRequests = List.of(requestDto);

        Collection<ItemRequestDto> targetRequests = service.getAllRequestsByUser(userId);

        assertThat(targetRequests, hasSize(sourceRequests.size()));
        for (ItemRequestDto sourceRequest : sourceRequests) {
            assertThat(targetRequests, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceRequest.getDescription()))
            )));
        }
    }

    @Test
    void getAllRequestsByUserNotExistingTest() {
        int userId = userDto.getId();

        assertThrows(UserNotFoundException.class, () -> service.createRequest(userId, requestDto));
    }

    @Test
    void getAllRequestsTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        service.createRequest(userId, requestDto);
        List<ItemRequestDto> sourceRequests = List.of();

        Collection<ItemRequestDto> targetRequests = service.getAllRequests(userId);

        assertThat(targetRequests, hasSize(sourceRequests.size()));
        for (ItemRequestDto sourceRequest : sourceRequests) {
            assertThat(targetRequests, hasItem(allOf(
                    hasProperty("id", nullValue())
            )));
        }
    }

    @Test
    void getRequestByIdTest() {
        UserDto user = userService.createUser(userDto);
        int userId = user.getId();
        ItemRequestDto request = service.createRequest(userId, requestDto);

        ItemRequestDto savedRequest = service.getRequestById(request.getId());

        assertThat(savedRequest, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("id", equalTo(savedRequest.getId())),
                hasProperty("description", equalTo(savedRequest.getDescription()))
        ));
    }

    @Test
    void getRequestByIdNotExistingTest() {
        int requestId = requestDto.getId();

        assertThrows(ItemRequestNotFoundException.class, () -> service.getRequestById(requestId));
    }
}
