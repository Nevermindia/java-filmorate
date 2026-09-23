package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

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

        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);

        assertEquals(1, violations.size());
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("Название не может быть пустым", violation.getMessage());
    }

    @Test
    void validate_WithDescriptionLength201_ShouldHaveViolation() {
        validFilm.setDescription("a".repeat(201));

        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);

        assertEquals(1, violations.size());
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("description", violation.getPropertyPath().toString());
        assertEquals("Максимальная длина описания — 200 символов", violation.getMessage());
    }

    @Test
    void validate_WithNegativeDuration_ShouldHaveViolation() {
        validFilm.setDuration(-10);

        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);

        assertEquals(1, violations.size());
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("duration", violation.getPropertyPath().toString());
        assertEquals("Продолжительность фильма должна быть положительным числом", violation.getMessage());
    }

    @Test
    void validate_WithZeroDuration_ShouldHaveViolation() {
        validFilm.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);

        assertEquals(1, violations.size());
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("duration", violation.getPropertyPath().toString());
        assertEquals("Продолжительность фильма должна быть положительным числом", violation.getMessage());
    }

    @Test
    void validate_WithNullReleaseDate_ShouldHaveViolation() {
        validFilm.setReleaseDate(null);

        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);

        assertEquals(1, violations.size());
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("releaseDate", violation.getPropertyPath().toString());
        assertEquals("Дата релиза не может быть пустой", violation.getMessage());
    }

    @Test
    void validate_WithValidFilm_ShouldHaveNoViolations() {
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);

        assertTrue(violations.isEmpty());
    }
}