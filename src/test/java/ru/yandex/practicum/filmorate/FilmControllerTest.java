package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        validFilm = new Film();
        validFilm.setName("Test Film");
        validFilm.setDescription("Test description");
        validFilm.setReleaseDate(LocalDate.of(2000, Month.JANUARY, 1));
        validFilm.setDuration(120);
    }

    @Test
    void createFilm_ShouldCreateValidFilm() {
        Film created = filmController.createFilm(validFilm);

        assertNotNull(created.getId());
        assertEquals("Test Film", created.getName());
    }

    @Test
    void createFilm_WithDescriptionLength200_ShouldCreateFilm() {
        validFilm.setDescription("a".repeat(200));

        Film created = filmController.createFilm(validFilm);

        assertNotNull(created);
        assertEquals(200, created.getDescription().length());
    }

    @Test
    void createFilm_WithReleaseDateLastForbidden_ShouldThrowValidationException() {
        validFilm.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 27));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(validFilm));

        assertEquals("Дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void createFilm_WithReleaseDateFirstAllowed_ShouldCreateFilm() {
        validFilm.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 28));

        Film created = filmController.createFilm(validFilm);

        assertNotNull(created);
        assertEquals(LocalDate.of(1895, Month.DECEMBER, 28), created.getReleaseDate());
    }

    @Test
    void updateFilm_ShouldUpdateExistingFilm() {
        Film created = filmController.createFilm(validFilm);

        Film updateData = new Film();
        updateData.setId(created.getId());
        updateData.setName("Updated Film");
        updateData.setDescription("Updated description");
        updateData.setReleaseDate(LocalDate.of(2020, Month.JANUARY, 1));
        updateData.setDuration(150);

        Film updated = filmController.updateFilm(updateData);

        assertEquals("Updated Film", updated.getName());
        assertEquals("Updated description", updated.getDescription());
    }

    @Test
    void updateFilm_WithNonExistentId_ShouldThrowRuntimeException() {
        Film updateData = new Film();
        updateData.setId(999);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> filmController.updateFilm(updateData));

        assertEquals("Фильм не найден с id: 999", exception.getMessage());
    }

    @Test
    void updateFilm_WithReleaseDateLastForbidden_ShouldThrowValidationException() {
        Film created = filmController.createFilm(validFilm);

        Film updateData = new Film();
        updateData.setId(created.getId());
        updateData.setName("Updated Film");
        updateData.setDescription("Updated description");
        updateData.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 27));
        updateData.setDuration(150);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.updateFilm(updateData));

        assertEquals("Дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void updateFilm_WithReleaseDateFirstAllowed_ShouldUpdateFilm() {
        Film created = filmController.createFilm(validFilm);

        Film updateData = new Film();
        updateData.setId(created.getId());
        updateData.setName("Updated Film");
        updateData.setDescription("Updated description");
        updateData.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 28));
        updateData.setDuration(150);

        Film updated = filmController.updateFilm(updateData);

        assertEquals(LocalDate.of(1895, Month.DECEMBER, 28), updated.getReleaseDate());
    }

    @Test
    void getFilms_ShouldReturnAllFilms() {
        filmController.createFilm(validFilm);

        Film secondFilm = new Film();
        secondFilm.setName("Second Film");
        secondFilm.setDescription("Second description");
        secondFilm.setReleaseDate(LocalDate.of(2021, Month.JANUARY, 1));
        secondFilm.setDuration(90);
        filmController.createFilm(secondFilm);

        List<Film> films = filmController.getFilms();

        assertEquals(2, films.size());
    }

    @Test
    void getFilms_WhenNoFilms_ShouldReturnEmptyList() {
        List<Film> films = filmController.getFilms();

        assertNotNull(films);
        assertTrue(films.isEmpty());
    }
}
