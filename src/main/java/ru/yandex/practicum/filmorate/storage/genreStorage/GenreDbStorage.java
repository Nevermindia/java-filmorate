package ru.yandex.practicum.filmorate.storage.genreStorage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Genre> genreRowMapper = new GenreRowMapper();

    @Override
    public List<Genre> getGenres() {
        return jdbcTemplate.query("SELECT id, name FROM genres ORDER BY id", genreRowMapper);
    }

    @Override
    public Genre getGenreById(Integer id) {
        List<Genre> genres = jdbcTemplate.query(
                "SELECT id, name FROM genres WHERE id = ?",
                genreRowMapper,
                id);
        return genres.isEmpty() ? null : genres.getFirst();
    }
}
