package ru.yandex.practicum.filmorate.storage.filmStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Film createFilm(Film film) {

        Long id = nextId++;
        film.setId(id);
        films.put(id, film);

        log.info("Film created successfully with id: {}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        Film existingFilm = getFilmById(film.getId());

        existingFilm.setName(film.getName());
        existingFilm.setDescription(film.getDescription());
        existingFilm.setReleaseDate(film.getReleaseDate());
        existingFilm.setDuration(film.getDuration());

        log.info("Film updated successfully with id: {}", existingFilm.getId());

        return existingFilm;
    }

    @Override
    public List<Film> getFilms() {
        log.debug("Returning all films, count: {}", films.size());
        List<Film> filmList = new ArrayList<>(films.values());
        return filmList;
    }

    @Override
    public Film getFilmById(Long id) {
        return films.get(id);
    }
}
