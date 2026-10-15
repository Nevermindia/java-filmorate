package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genreStorage.GenreStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage storage;

    public List<Genre> getGenres() {
        return storage.getGenres();
    }

    public Genre getGenreById(Integer id) {
        Genre genre = storage.getGenreById(id);
        if (genre == null) {
            throw new NotFoundException("Жанр с таким id не найден: " + id);
        }
        return genre;
    }
}
