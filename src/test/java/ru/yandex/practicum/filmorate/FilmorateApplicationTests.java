package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void userStorage_ShouldPerformCrudOperations() {
        User created = userStorage.createUser(createUser("first"));

        assertNotNull(created.getId());
        assertEquals(created, userStorage.getUserById(created.getId()));
        assertEquals(1, userStorage.getUsers().size());

        created.setName("Updated name");
        User updated = userStorage.updateUser(created);

        assertEquals("Updated name", updated.getName());
        assertNull(userStorage.getUserById(999L));
    }

    @Test
    void userStorage_ShouldManageOneWayFriendships() {
        User first = userStorage.createUser(createUser("first"));
        User second = userStorage.createUser(createUser("second"));
        User common = userStorage.createUser(createUser("common"));

        userStorage.addToFriend(first.getId(), common.getId());
        userStorage.addToFriend(second.getId(), common.getId());

        assertEquals(List.of(common), userStorage.getFriends(first.getId()));
        assertTrue(userStorage.getFriends(common.getId()).isEmpty());
        assertEquals(List.of(common), userStorage.getFriendsInCommon(first.getId(), second.getId()));

        userStorage.removeFromFriends(first.getId(), common.getId());
        assertTrue(userStorage.getFriends(first.getId()).isEmpty());
    }

    @Test
    void filmStorage_ShouldPerformCrudAndLikeOperations() {
        User firstUser = userStorage.createUser(createUser("first"));
        User secondUser = userStorage.createUser(createUser("second"));
        Mpa mpa = mpaStorage.getMpaById(1);
        Genre genre = genreStorage.getGenreById(1);

        Film firstFilm = filmStorage.createFilm(createFilm("First", mpa, genre));
        Film secondFilm = filmStorage.createFilm(createFilm("Second", mpa, genre));

        assertNotNull(firstFilm.getId());
        assertEquals(firstFilm, filmStorage.getFilmById(firstFilm.getId()));
        assertEquals(2, filmStorage.getFilms().size());

        firstFilm.setName("Updated film");
        assertEquals("Updated film", filmStorage.updateFilm(firstFilm).getName());

        filmStorage.addLike(firstFilm.getId(), firstUser.getId());
        filmStorage.addLike(firstFilm.getId(), secondUser.getId());
        filmStorage.addLike(secondFilm.getId(), firstUser.getId());

        assertEquals(firstFilm.getId(), filmStorage.getMostPopularFilms(1).getFirst().getId());
        assertEquals(2, filmStorage.getFilmById(firstFilm.getId()).getLikes().size());

        filmStorage.removeLike(firstFilm.getId(), secondUser.getId());
        assertEquals(1, filmStorage.getFilmById(firstFilm.getId()).getLikes().size());
        assertNull(filmStorage.getFilmById(999L));
    }

    @Test
    void referenceStorages_ShouldReturnSeedData() {
        assertEquals(6, genreStorage.getGenres().size());
        assertEquals("Комедия", genreStorage.getGenreById(1).getName());
        assertNull(genreStorage.getGenreById(999));

        assertEquals(5, mpaStorage.getMpaRatings().size());
        assertEquals("G", mpaStorage.getMpaById(1).getName());
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

    private Film createFilm(String name, Mpa mpa, Genre genre) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);
        film.setGenres(new LinkedHashSet<>(List.of(genre)));
        return film;
    }
}
