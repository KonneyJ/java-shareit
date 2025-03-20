package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

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
public class UserServiceImplTest {
    private final EntityManager em;
    private final UserService service;

    private UserDto userDto;
    private UserDto userDto2;

    @BeforeEach
    public void setUp() {
        userDto = new UserDto();
        userDto.setName("Julie");
        userDto.setEmail("julie17@yandex.ru");

        userDto2 = new UserDto();
        userDto2.setName("Dima");
        userDto2.setEmail("dima27@yandex.ru");
    }

    @Test
    void createUserTest() {
        service.createUser(userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail())
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void updateUserTest() {
        UserDto savedUser = service.createUser(userDto);
        int userId = savedUser.getId();
        service.updateUser(userId, userDto2);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :user_id", User.class);
        User user = query.setParameter("user_id", userId)
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getId(), equalTo(userId));
        assertThat(user.getName(), equalTo(userDto2.getName()));
        assertThat(user.getEmail(), equalTo(userDto2.getEmail()));
    }

    @Test
    void updateUserNotExistingTest() {
        int userId = userDto.getId();

        assertThrows(UserNotFoundException.class, () -> service.updateUser(userId, userDto2));
    }

    @Test
    void getUserByIdTest() {
        UserDto savedUser = service.createUser(userDto);

        UserDto user = service.getUserById(savedUser.getId());

        assertThat(user, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(user.getName())),
                hasProperty("email", equalTo(user.getEmail()))
        ));
    }

    @Test
    void getUserByNotExistingIdTest() {
        int userId = userDto.getId();

        assertThrows(UserNotFoundException.class, () -> service.getUserById(userId));
    }

    @Test
    void getAllUsersTest() {
        List<UserDto> sourceUsers = List.of(userDto, userDto2);

        for (UserDto user : sourceUsers) {
            User entity = UserMapper.toUser(user);
            em.persist(entity);
        }
        em.flush();

        Collection<UserDto> targetUsers = service.getAllUsers();

        assertThat(targetUsers, hasSize(sourceUsers.size()));
        for (UserDto sourceUser : sourceUsers) {
            assertThat(targetUsers, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceUser.getName())),
                    hasProperty("email", equalTo(sourceUser.getEmail()))
            )));
        }
    }

    @Test
    void deleteUserTest() {
        UserDto savedUser = service.createUser(userDto);
        int userId = savedUser.getId();

        service.deleteUser(userId);

        assertThrows(UserNotFoundException.class, () -> service.getUserById(userId));
    }
}
