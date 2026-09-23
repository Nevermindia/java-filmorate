package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        userController = new UserController();
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
    void updateUser_WithNonExistentId_ShouldThrowRuntimeException() {
        User updateData = new User();
        updateData.setId(999);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userController.updateUser(updateData));

        assertEquals("User not found with id: 999", exception.getMessage());
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

        ArrayList<User> users = userController.getUsers();

        assertEquals(2, users.size());
    }

    @Test
    void getUsers_WhenNoUsers_ShouldReturnEmptyList() {
        ArrayList<User> users = userController.getUsers();

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
}