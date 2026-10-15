package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.filmStorage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genreStorage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpaStorage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.userStorage.UserDbStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;

    @Test
    void createUser_ShouldSaveUser() {
        User created = userStorage.createUser(createUser("first"));

        assertAll(
                () -> assertNotNull(created.getId()),
                () -> assertEquals("first@example.com", created.getEmail()),
                () -> assertEquals("first", created.getLogin()));
    }

    @Test
    void getUserById_ShouldReturnSavedUser() {
        User created = userStorage.createUser(createUser("first"));

        User found = userStorage.getUserById(created.getId());

        assertAll(
                () -> assertEquals(created.getId(), found.getId()),
                () -> assertEquals(created.getEmail(), found.getEmail()),
                () -> assertEquals(created.getLogin(), found.getLogin()));
    }

    @Test
    void getUserById_ShouldReturnNullForUnknownId() {
        assertNull(userStorage.getUserById(999L));
    }

    @Test
    void getUsers_ShouldReturnAllUsers() {
        User first = userStorage.createUser(createUser("first"));
        User second = userStorage.createUser(createUser("second"));

        assertEquals(List.of(first, second), userStorage.getUsers());
    }

    @Test
    void updateUser_ShouldChangeUserData() {
        User created = userStorage.createUser(createUser("first"));
        created.setName("Updated name");

        User updated = userStorage.updateUser(created);

        assertEquals("Updated name", updated.getName());
    }

    @Test
    void updateUser_ShouldRejectUnknownId() {
        User user = createUser("first");
        user.setId(999L);

        assertThrows(NotFoundException.class, () -> userStorage.updateUser(user));
    }

    @Test
    void addToFriend_ShouldCreateOneWayFriendship() {
        User first = userStorage.createUser(createUser("first"));
        User second = userStorage.createUser(createUser("second"));

        userStorage.addToFriend(first.getId(), second.getId());

        assertAll(
                () -> assertEquals(List.of(second), userStorage.getFriends(first.getId())),
                () -> assertTrue(userStorage.getFriends(second.getId()).isEmpty()));
    }

    @Test
    void removeFromFriends_ShouldDeleteFriendship() {
        User first = userStorage.createUser(createUser("first"));
        User second = userStorage.createUser(createUser("second"));
        userStorage.addToFriend(first.getId(), second.getId());

        userStorage.removeFromFriends(first.getId(), second.getId());

        assertTrue(userStorage.getFriends(first.getId()).isEmpty());
    }

    @Test
    void getFriendsInCommon_ShouldReturnIntersection() {
        User first = userStorage.createUser(createUser("first"));
        User second = userStorage.createUser(createUser("second"));
        User common = userStorage.createUser(createUser("common"));

        userStorage.addToFriend(first.getId(), common.getId());
        userStorage.addToFriend(second.getId(), common.getId());

        assertEquals(List.of(common), userStorage.getFriendsInCommon(first.getId(), second.getId()));
    }

    @Test
    void createUser_ShouldRejectDuplicateEmail() {
        User first = userStorage.createUser(createUser("first"));
        User duplicateEmail = createUser("second");
        duplicateEmail.setEmail(first.getEmail());

        assertThrows(ValidationException.class, () -> userStorage.createUser(duplicateEmail));
    }

    @Test
    void createUser_ShouldRejectDuplicateLogin() {
        userStorage.createUser(createUser("first"));
        User duplicateLogin = createUser("first");
        duplicateLogin.setEmail("unique@example.com");

        assertThrows(ValidationException.class, () -> userStorage.createUser(duplicateLogin));
    }

    @Test
    void updateUser_ShouldRejectDuplicateEmail() {
        User first = userStorage.createUser(createUser("first"));
        User second = userStorage.createUser(createUser("second"));
        second.setEmail(first.getEmail());

        assertThrows(ValidationException.class, () -> userStorage.updateUser(second));
    }

    @Test
    void createFilm_ShouldSaveFilm() {
        Mpa mpa = mpaStorage.getMpaById(1);
        Genre genre = genreStorage.getGenreById(1);
        Film created = filmStorage.createFilm(createFilm("First", mpa, genre));

        assertAll(
                () -> assertNotNull(created.getId()),
                () -> assertEquals("First", created.getName()),
                () -> assertEquals(mpa, created.getMpa()),
                () -> assertEquals(List.of(genre), created.getGenres().stream().toList()));
    }

    @Test
    void getFilmById_ShouldReturnSavedFilm() {
        Mpa mpa = mpaStorage.getMpaById(1);
        Genre genre = genreStorage.getGenreById(1);
        Film created = filmStorage.createFilm(createFilm("First", mpa, genre));

        Film found = filmStorage.getFilmById(created.getId());

        assertAll(
                () -> assertEquals(created.getId(), found.getId()),
                () -> assertEquals("First", found.getName()),
                () -> assertEquals(List.of(genre), found.getGenres().stream().toList()));
    }

    @Test
    void getFilmById_ShouldReturnNullForUnknownId() {
        assertNull(filmStorage.getFilmById(999L));
    }

    @Test
    void getFilms_ShouldReturnAllFilms() {
        Mpa mpa = mpaStorage.getMpaById(1);
        Genre genre = genreStorage.getGenreById(1);
        Film first = filmStorage.createFilm(createFilm("First", mpa, genre));
        Film second = filmStorage.createFilm(createFilm("Second", mpa, genre));

        assertEquals(List.of(first, second), filmStorage.getFilms());
    }

    @Test
    void updateFilm_ShouldChangeFilmData() {
        Mpa mpa = mpaStorage.getMpaById(1);
        Genre genre = genreStorage.getGenreById(1);
        Film film = filmStorage.createFilm(createFilm("First", mpa, genre));
        film.setName("Updated film");

        Film updated = filmStorage.updateFilm(film);

        assertEquals("Updated film", updated.getName());
    }

    @Test
    void updateFilm_ShouldReplaceGenres() {
        Mpa mpa = mpaStorage.getMpaById(1);
        Genre oldGenre = genreStorage.getGenreById(1);
        Genre firstNewGenre = genreStorage.getGenreById(2);
        Genre secondNewGenre = genreStorage.getGenreById(3);
        Film film = filmStorage.createFilm(createFilm("First", mpa, oldGenre));
        film.setGenres(new LinkedHashSet<>(List.of(firstNewGenre, secondNewGenre)));

        Film updated = filmStorage.updateFilm(film);

        assertEquals(List.of(firstNewGenre, secondNewGenre), updated.getGenres().stream().toList());
    }

    @Test
    void updateFilm_ShouldRejectUnknownId() {
        Mpa mpa = mpaStorage.getMpaById(1);
        Genre genre = genreStorage.getGenreById(1);
        Film film = createFilm("First", mpa, genre);
        film.setId(999L);

        assertThrows(NotFoundException.class, () -> filmStorage.updateFilm(film));
    }

    @Test
    void createFilm_ShouldRemoveDuplicateGenreIds() {
        Mpa mpa = mpaStorage.getMpaById(1);
        Genre genre = genreStorage.getGenreById(1);
        Genre duplicate = new Genre();
        duplicate.setId(genre.getId());
        duplicate.setName("Duplicate name");

        Film created = filmStorage.createFilm(createFilm("First", mpa, genre, duplicate));

        assertAll(
                () -> assertEquals(1, created.getGenres().size()),
                () -> assertEquals(genre.getId(), created.getGenres().iterator().next().getId()));
    }

    @Test
    void createFilm_ShouldRejectUnknownMpa() {
        Mpa unknownMpa = new Mpa();
        unknownMpa.setId(999);
        Genre genre = genreStorage.getGenreById(1);
        Film film = createFilm("First", unknownMpa, genre);

        assertThrows(NotFoundException.class, () -> filmStorage.createFilm(film));
    }

    @Test
    void createFilm_ShouldRejectUnknownGenre() {
        Mpa mpa = mpaStorage.getMpaById(1);
        Genre unknownGenre = new Genre();
        unknownGenre.setId(999);
        Film film = createFilm("First", mpa, unknownGenre);

        assertThrows(NotFoundException.class, () -> filmStorage.createFilm(film));
    }

    @Test
    void addLike_ShouldSaveLike() {
        User user = userStorage.createUser(createUser("first"));
        Mpa mpa = mpaStorage.getMpaById(1);
        Genre genre = genreStorage.getGenreById(1);
        Film film = filmStorage.createFilm(createFilm("First", mpa, genre));

        filmStorage.addLike(film.getId(), user.getId());

        assertTrue(filmStorage.getFilmById(film.getId()).getLikes().contains(user.getId()));
    }

    @Test
    void removeLike_ShouldDeleteLike() {
        User user = userStorage.createUser(createUser("first"));
        Mpa mpa = mpaStorage.getMpaById(1);
        Genre genre = genreStorage.getGenreById(1);
        Film film = filmStorage.createFilm(createFilm("First", mpa, genre));
        filmStorage.addLike(film.getId(), user.getId());

        filmStorage.removeLike(film.getId(), user.getId());

        assertTrue(filmStorage.getFilmById(film.getId()).getLikes().isEmpty());
    }

    @Test
    void getMostPopularFilms_ShouldSortFilmsByLikes() {
        User firstUser = userStorage.createUser(createUser("first"));
        User secondUser = userStorage.createUser(createUser("second"));
        Mpa mpa = mpaStorage.getMpaById(1);
        Genre genre = genreStorage.getGenreById(1);
        Film firstFilm = filmStorage.createFilm(createFilm("First", mpa, genre));
        Film secondFilm = filmStorage.createFilm(createFilm("Second", mpa, genre));

        filmStorage.addLike(secondFilm.getId(), firstUser.getId());
        filmStorage.addLike(secondFilm.getId(), secondUser.getId());
        filmStorage.addLike(firstFilm.getId(), firstUser.getId());

        assertEquals(secondFilm.getId(), filmStorage.getMostPopularFilms(1).getFirst().getId());
    }

    @Test
    void getGenres_ShouldReturnAllSeedGenres() {
        assertEquals(6, genreStorage.getGenres().size());
    }

    @Test
    void getGenreById_ShouldReturnGenre() {
        assertEquals("Комедия", genreStorage.getGenreById(1).getName());
    }

    @Test
    void getGenreById_ShouldReturnNullForUnknownId() {
        assertNull(genreStorage.getGenreById(999));
    }

    @Test
    void getMpaRatings_ShouldReturnAllSeedRatings() {
        assertEquals(5, mpaStorage.getMpaRatings().size());
    }

    @Test
    void getMpaById_ShouldReturnRating() {
        assertEquals("G", mpaStorage.getMpaById(1).getName());
    }

    @Test
    void getMpaById_ShouldReturnNullForUnknownId() {
        assertNull(mpaStorage.getMpaById(999));
    }

    private User createUser(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    private Film createFilm(String name, Mpa mpa, Genre... genres) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);
        film.setGenres(new LinkedHashSet<>(List.of(genres)));
        return film;
    }
}
