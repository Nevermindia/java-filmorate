package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {

    private Validator validator;
    private User validUser;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validUser = new User();
        validUser.setEmail("test@example.com");
        validUser.setLogin("testuser");
        validUser.setName("Test User");
        validUser.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void validate_WithEmptyEmail_ShouldHaveViolation() {
        validUser.setEmail("");

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        assertEquals(1, violations.size());
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Электронная почта не может быть пустой", violation.getMessage());
    }

    @Test
    void validate_WithEmailWithoutAt_ShouldHaveViolation() {
        validUser.setEmail("testexample.com");

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        assertEquals(1, violations.size());
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Электронная почта должна содержать символ @", violation.getMessage());
    }

    @Test
    void validate_WithEmptyLogin_ShouldHaveViolation() {
        validUser.setLogin("");

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        assertEquals(1, violations.size());
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("login", violation.getPropertyPath().toString());
        assertEquals("Логин не может быть пустым", violation.getMessage());
    }

    @Test
    void validate_WithLoginContainingSpaces_ShouldHaveViolation() {
        validUser.setLogin("test user");

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        assertEquals(1, violations.size());
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("login", violation.getPropertyPath().toString());
        assertEquals("Логин не может содержать пробелы", violation.getMessage());
    }

    @Test
    void validate_WithNullBirthday_ShouldHaveViolation() {
        validUser.setBirthday(null);

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        assertEquals(1, violations.size());
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("birthday", violation.getPropertyPath().toString());
        assertEquals("Дата рождения не может быть пустой", violation.getMessage());
    }

    @Test
    void validate_WithBirthdayInFuture_ShouldHaveViolation() {
        validUser.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        assertEquals(1, violations.size());
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("birthday", violation.getPropertyPath().toString());
        assertEquals("Дата рождения не может быть в будущем", violation.getMessage());
    }

    @Test
    void validate_WithValidUser_ShouldHaveNoViolations() {
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        assertTrue(violations.isEmpty());
    }
}