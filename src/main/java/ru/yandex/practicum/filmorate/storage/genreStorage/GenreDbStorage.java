package ru.yandex.practicum.filmorate.storage.genreStorage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getGenres() {
        return jdbcTemplate.query("SELECT id, name FROM genres ORDER BY id", (resultSet, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(resultSet.getInt("id"));
            genre.setName(resultSet.getString("name"));
            return genre;
        });
    }

    @Override
    public Genre getGenreById(Integer id) {
        List<Genre> genres = jdbcTemplate.query(
                "SELECT id, name FROM genres WHERE id = ?",
                (resultSet, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(resultSet.getInt("id"));
                    genre.setName(resultSet.getString("name"));
                    return genre;
                },
                id);
        return genres.isEmpty() ? null : genres.getFirst();
    }
}
