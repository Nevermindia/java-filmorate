package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

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
        assertFalse(validator.validate(validUser).isEmpty());
    }

    @Test
    void validate_WithEmailWithoutAt_ShouldHaveViolation() {
        validUser.setEmail("testexample.com");
        assertFalse(validator.validate(validUser).isEmpty());
    }

    @Test
    void validate_WithEmptyLogin_ShouldHaveViolation() {
        validUser.setLogin("");
        assertFalse(validator.validate(validUser).isEmpty());
    }

    @Test
    void validate_WithLoginContainingSpaces_ShouldHaveViolation() {
        validUser.setLogin("test user");
        assertFalse(validator.validate(validUser).isEmpty());
    }

    @Test
    void validate_WithBirthdayInFuture_ShouldHaveViolation() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        assertFalse(validator.validate(validUser).isEmpty());
    }

    @Test
    void validate_WithValidUser_ShouldHaveNoViolations() {
        assertTrue(validator.validate(validUser).isEmpty());
    }
}