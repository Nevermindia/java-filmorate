package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.filmStorage.FilmStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static java.time.Month.DECEMBER;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage storage;
    private final UserService userService;

    public Film createFilm(Film film) {
        validateReleaseDate(film);
        return storage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        getFilmById(film.getId());
        validateReleaseDate(film);
        return storage.updateFilm(film);
    }

    public List<Film> getFilms() {
        return storage.getFilms();
    }

    public Film getFilmById(Long id) {
        Film filmById = storage.getFilmById(id);
        if (filmById == null) {
            throw new NotFoundException("Фильма с таким id не найден: " + id);
        }
        return filmById;
    }

    public void addLike(Long filmId, Long userId) {
        userService.getUserById(userId);
        getFilmById(filmId).getLikes().add(userId);
        log.info("User {} set like for film {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        userService.getUserById(userId);
        getFilmById(filmId).getLikes().remove(userId);
        log.info("User {} deleted like for film {}", userId, filmId);
    }

    public List<Film> getMostPopularFilms(int count) {
        if (count < 0) {
            throw new ValidationException("Значение count должно быть больше или равно 0");
        }
        log.debug("Getting {} most popular films", count);
        return storage.getFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    private static void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, DECEMBER, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }
}
