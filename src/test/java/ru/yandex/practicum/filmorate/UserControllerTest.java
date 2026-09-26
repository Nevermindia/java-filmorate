package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.userStorage.InMemoryUserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        userController = new UserController(new UserService(new InMemoryUserStorage()));
        validUser = new User();
        validUser.setEmail("test@example.com");
        validUser.setLogin("testuser");
        validUser.setName("Test User");
        validUser.setBirthday(LocalDate.of(2000, Month.JANUARY, 1));
    }

    @Test
    void createUser_ShouldReturnCreatedUserWithId() {
        User created = userController.createUser(validUser);

        assertNotNull(created.getId());
        assertEquals("test@example.com", created.getEmail());
        assertEquals("testuser", created.getLogin());
    }

    @Test
    void createUser_WithEmptyName_ShouldSetLoginAsName() {
        validUser.setName("");

        User created = userController.createUser(validUser);

        assertEquals("testuser", created.getName());
    }

    @Test
    void createUser_WithNullName_ShouldSetLoginAsName() {
        validUser.setName(null);

        User created = userController.createUser(validUser);

        assertEquals("testuser", created.getName());
    }

    @Test
    void createUser_WithBirthdayToday_ShouldCreateUser() {
        validUser.setBirthday(LocalDate.now());

        User created = userController.createUser(validUser);

        assertNotNull(created);
        assertEquals(LocalDate.now(), created.getBirthday());
    }

    @Test
    void updateUser_ShouldUpdateExistingUserFields() {
        User created = userController.createUser(validUser);

        User updateData = new User();
        updateData.setId(created.getId());
        updateData.setEmail("updated@example.com");
        updateData.setLogin("updateduser");
        updateData.setName("Updated User");
        updateData.setBirthday(LocalDate.of(1995, Month.JANUARY, 1));

        User updated = userController.updateUser(updateData);

        assertEquals("updated@example.com", updated.getEmail());
        assertEquals("updateduser", updated.getLogin());
    }

    @Test
    void updateUser_WithNonExistentId_ShouldThrowNotFoundException() {
        User updateData = new User();
        updateData.setId(999L);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userController.updateUser(updateData));

        assertEquals("Пользователя с таким id не существует: 999", exception.getMessage());
    }

    @Test
    void getUsers_ShouldReturnListOfAllUsers() {
        userController.createUser(validUser);

        User secondUser = new User();
        secondUser.setEmail("second@example.com");
        secondUser.setLogin("seconduser");
        secondUser.setName("Second User");
        secondUser.setBirthday(LocalDate.of(2001, Month.JANUARY, 1));
        userController.createUser(secondUser);

        List<User> users = userController.getUsers();

        assertEquals(2, users.size());
    }

    @Test
    void getUsers_WhenNoUsers_ShouldReturnEmptyList() {
        List<User> users = userController.getUsers();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void updateUser_WithNullName_ShouldSetLoginAsName() {
        User created = userController.createUser(validUser);

        User updateData = new User();
        updateData.setId(created.getId());
        updateData.setEmail("updated@example.com");
        updateData.setLogin("updateduser");
        updateData.setName(null);  // ← ключевой случай
        updateData.setBirthday(LocalDate.of(1995, Month.JANUARY, 1));

        User updated = userController.updateUser(updateData);

        assertEquals("updateduser", updated.getName());
    }

    @Test
    void updateUser_WithBlankName_ShouldSetLoginAsName() {
        User created = userController.createUser(validUser);

        User updateData = new User();
        updateData.setId(created.getId());
        updateData.setEmail("updated@example.com");
        updateData.setLogin("updateduser");
        updateData.setName("   ");
        updateData.setBirthday(LocalDate.of(1995, Month.JANUARY, 1));

        User updated = userController.updateUser(updateData);

        assertEquals("updateduser", updated.getName());
    }

    @Test
    void getUserById_ShouldReturnExistingUser() {
        User createdUser = userController.createUser(validUser);

        User foundUser = userController.getUserById(createdUser.getId());

        assertEquals(createdUser, foundUser);
    }

    @Test
    void getUserById_WithNonExistentId_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class,
                () -> userController.getUserById(999L));
    }

    @Test
    void addToFriends_ShouldAddFriendToUserFriends() {
        User firstUser = userController.createUser(validUser);
        User secondUser = userController.createUser(createUser("second"));

        userController.addToFriends(firstUser.getId(), secondUser.getId());

        List<User> friends = userController.getFriends(firstUser.getId());
        assertEquals(1, friends.size());
        assertEquals(secondUser.getId(), friends.getFirst().getId());
    }

    @Test
    void addToFriends_WithNonExistentFriend_ShouldThrowNotFoundException() {
        User firstUser = userController.createUser(validUser);

        assertThrows(NotFoundException.class,
                () -> userController.addToFriends(firstUser.getId(), 999L));
    }

    @Test
    void removeFromFriends_ShouldRemoveFriend() {
        User firstUser = userController.createUser(validUser);
        User secondUser = userController.createUser(createUser("second"));
        userController.addToFriends(firstUser.getId(), secondUser.getId());

        userController.removeFromFriends(firstUser.getId(), secondUser.getId());

        assertTrue(userController.getFriends(firstUser.getId()).isEmpty());
    }

    @Test
    void getCommonFriends_ShouldReturnOnlyCommonFriends() {
        User firstUser = userController.createUser(validUser);
        User secondUser = userController.createUser(createUser("second"));
        User commonFriend = userController.createUser(createUser("common"));

        userController.addToFriends(firstUser.getId(), commonFriend.getId());
        userController.addToFriends(secondUser.getId(), commonFriend.getId());

        List<User> commonFriends = userController.getCommonFriends(firstUser.getId(), secondUser.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(commonFriend.getId(), commonFriends.getFirst().getId());
    }

    private User createUser(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(2000, Month.JANUARY, 1));
        return user;
    }
}
