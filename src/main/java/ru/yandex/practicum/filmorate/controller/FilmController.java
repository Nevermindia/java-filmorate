package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.Month.DECEMBER;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private Integer nextId = 1;

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.debug("Received film data: {}", film);

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, DECEMBER, 28))) {
            log.warn("Validation failed: Release date {} is before 1895-12-28", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        Integer id = nextId++;
        film.setId(id);
        films.put(id, film);

        log.info("Film created successfully with id: {}", film.getId());
        log.debug("Created film: {}", film);

        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.debug("Received update request for film: {}", film);

        Integer id = film.getId();
        log.debug("Looking for film with id: {}", id);

        Film existingFilm = films.get(id);
        if (existingFilm == null) {
            log.error("Film not found with id: {}", id);
            throw new NotFoundException("Фильм не найден с id: " + id);
        }

        log.debug("Found existing film: {}", existingFilm);

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, DECEMBER, 28))) {
            log.warn("Validation failed: Release date {} is before 1895-12-28", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        log.debug("Updating name from '{}' to '{}'", existingFilm.getName(), film.getName());
        existingFilm.setName(film.getName());

        log.debug("Updating description from '{}' to '{}'", existingFilm.getDescription(), film.getDescription());
        existingFilm.setDescription(film.getDescription());

        log.debug("Updating release date from '{}' to '{}'", existingFilm.getReleaseDate(), film.getReleaseDate());
        existingFilm.setReleaseDate(film.getReleaseDate());

        log.debug("Updating duration from '{}' to '{}'", existingFilm.getDuration(), film.getDuration());
        existingFilm.setDuration(film.getDuration());

        log.info("Film updated successfully with id: {}", existingFilm.getId());
        log.debug("Updated film: {}", existingFilm);

        return existingFilm;
    }

    @GetMapping
    public List<Film> getFilms() {
        log.info("Returning all films, count: {}", films.size());
        List<Film> filmList = new ArrayList<>(films.values());
        log.debug("Films list: {}", filmList);
        return filmList;
    }
}