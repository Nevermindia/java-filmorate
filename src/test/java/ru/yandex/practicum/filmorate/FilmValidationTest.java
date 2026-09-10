package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {

    private Validator validator;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validFilm = new Film();
        validFilm.setName("Test Film");
        validFilm.setDescription("Test description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void validate_WithEmptyName_ShouldHaveViolation() {
        validFilm.setName("");
        assertFalse(validator.validate(validFilm).isEmpty());
    }

    @Test
    void validate_WithDescriptionLength201_ShouldHaveViolation() {
        validFilm.setDescription("a".repeat(201));
        assertFalse(validator.validate(validFilm).isEmpty());
    }

    @Test
    void validate_WithNegativeDuration_ShouldHaveViolation() {
        validFilm.setDuration(-10);
        assertFalse(validator.validate(validFilm).isEmpty());
    }

    @Test
    void validate_WithNullReleaseDate_ShouldHaveViolation() {
        validFilm.setReleaseDate(null);
        assertFalse(validator.validate(validFilm).isEmpty());
    }

    @Test
    void validate_WithValidFilm_ShouldHaveNoViolations() {
        assertTrue(validator.validate(validFilm).isEmpty());
    }
}