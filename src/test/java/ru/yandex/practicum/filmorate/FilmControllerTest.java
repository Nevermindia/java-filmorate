package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.filmStorage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.userStorage.InMemoryUserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;
    private UserService userService;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());
        filmController = new FilmController(new FilmService(new InMemoryFilmStorage(), userService));
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
    void updateFilm_WithNonExistentId_ShouldThrowNotFoundException() {
        Film updateData = new Film();
        updateData.setId(999L);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmController.updateFilm(updateData));

        assertEquals("Фильма с таким id не найден: 999", exception.getMessage());
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

    @Test
    void addLike_ShouldAddUserIdToFilmLikes() {
        Film createdFilm = filmController.createFilm(validFilm);
        User createdUser = userService.createUser(createUser("first"));

        filmController.addLike(createdFilm.getId(), createdUser.getId());

        assertTrue(createdFilm.getLikes().contains(createdUser.getId()));
    }

    @Test
    void addLike_WithNonExistentUser_ShouldThrowNotFoundException() {
        Film createdFilm = filmController.createFilm(validFilm);

        assertThrows(NotFoundException.class,
                () -> filmController.addLike(createdFilm.getId(), 999L));
    }

    @Test
    void addLike_WithNonExistentFilm_ShouldThrowNotFoundException() {
        User createdUser = userService.createUser(createUser("first"));

        assertThrows(NotFoundException.class,
                () -> filmController.addLike(999L, createdUser.getId()));
    }

    @Test
    void removeLike_ShouldRemoveUserIdFromFilmLikes() {
        Film createdFilm = filmController.createFilm(validFilm);
        User createdUser = userService.createUser(createUser("first"));
        filmController.addLike(createdFilm.getId(), createdUser.getId());

        filmController.removeLike(createdFilm.getId(), createdUser.getId());

        assertFalse(createdFilm.getLikes().contains(createdUser.getId()));
    }

    @Test
    void getMostPopularFilms_ShouldRespectCountAndSortByLikesDescending() {
        Film firstFilm = filmController.createFilm(validFilm);
        Film secondFilm = filmController.createFilm(createFilm("Second Film"));
        Film thirdFilm = filmController.createFilm(createFilm("Third Film"));
        User firstUser = userService.createUser(createUser("first"));
        User secondUser = userService.createUser(createUser("second"));

        filmController.addLike(secondFilm.getId(), firstUser.getId());
        filmController.addLike(thirdFilm.getId(), firstUser.getId());
        filmController.addLike(thirdFilm.getId(), secondUser.getId());

        List<Film> popularFilms = filmController.getMostPopularFilms(2);

        assertEquals(2, popularFilms.size());
        assertEquals(thirdFilm.getId(), popularFilms.get(0).getId());
        assertEquals(secondFilm.getId(), popularFilms.get(1).getId());
        assertFalse(popularFilms.contains(firstFilm));
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, Month.JANUARY, 1));
        film.setDuration(120);
        return film;
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
