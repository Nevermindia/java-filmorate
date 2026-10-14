package ru.yandex.practicum.filmorate.storage.filmStorage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Primary
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String SELECT_FILMS = "SELECT f.id, f.name, f.description, f.release_date, "
            + "f.duration, f.mpa_id, m.name AS mpa_name FROM films f "
            + "LEFT JOIN mpa m ON f.mpa_id = m.id";

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Film> filmRowMapper = this::mapFilm;

    @Override
    @Transactional
    public Film createFilm(Film film) {
        validateReferences(film);
        Map<String, Object> values = new HashMap<>();
        values.put("name", film.getName());
        values.put("description", film.getDescription());
        values.put("release_date", film.getReleaseDate());
        values.put("duration", film.getDuration());
        values.put("mpa_id", film.getMpa() == null ? null : film.getMpa().getId());

        Number id = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id")
                .executeAndReturnKey(values);
        saveGenres(id.longValue(), film.getGenres());
        return getFilmById(id.longValue());
    }

    @Override
    @Transactional
    public Film updateFilm(Film film) {
        validateReferences(film);
        Integer mpaId = film.getMpa() == null ? null : film.getMpa().getId();
        int updatedRows = jdbcTemplate.update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? "
                        + "WHERE id = ?",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                mpaId, film.getId());
        if (updatedRows == 0) {
            throw new NotFoundException("Фильма с таким id не найден: " + film.getId());
        }
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film.getId(), film.getGenres());
        return getFilmById(film.getId());
    }

    @Override
    public List<Film> getFilms() {
        List<Film> films = jdbcTemplate.query(SELECT_FILMS + " ORDER BY f.id", filmRowMapper);
        films.forEach(this::loadRelations);
        return films;
    }

    @Override
    public Film getFilmById(Long id) {
        List<Film> films = jdbcTemplate.query(SELECT_FILMS + " WHERE f.id = ?", filmRowMapper, id);
        if (films.isEmpty()) {
            return null;
        }
        Film film = films.getFirst();
        loadRelations(film);
        return film;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)",
                filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        String sql = SELECT_FILMS
                + " LEFT JOIN film_likes fl ON f.id = fl.film_id"
                + " GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name"
                + " ORDER BY COUNT(fl.user_id) DESC, f.id LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);
        films.forEach(this::loadRelations);
        return films;
    }

    private Film mapFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));
        Integer mpaId = resultSet.getObject("mpa_id", Integer.class);
        if (mpaId != null) {
            Mpa mpa = new Mpa();
            mpa.setId(mpaId);
            mpa.setName(resultSet.getString("mpa_name"));
            film.setMpa(mpa);
        }
        return film;
    }

    private void loadRelations(Film film) {
        String genreSql = "SELECT g.id, g.name FROM genres g "
                + "JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.id";
        List<Genre> genres = jdbcTemplate.query(genreSql, (resultSet, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(resultSet.getInt("id"));
            genre.setName(resultSet.getString("name"));
            return genre;
        }, film.getId());
        film.setGenres(new LinkedHashSet<>(genres));

        List<Long> likes = jdbcTemplate.queryForList(
                "SELECT user_id FROM film_likes WHERE film_id = ? ORDER BY user_id",
                Long.class,
                film.getId());
        film.setLikes(new HashSet<>(likes));
    }

    private void saveGenres(Long filmId, Set<Genre> genres) {
        if (genres == null) {
            return;
        }
        genres.forEach(genre -> jdbcTemplate.update(
                "MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) VALUES (?, ?)",
                filmId, genre.getId()));
    }

    private void validateReferences(Film film) {
        if (film.getMpa() != null) {
            ensureReferenceExists("mpa", film.getMpa().getId(), "Рейтинг MPA");
        }
        if (film.getGenres() != null) {
            film.getGenres().forEach(genre ->
                    ensureReferenceExists("genres", genre.getId(), "Жанр"));
        }
    }

    private void ensureReferenceExists(String table, Integer id, String entityName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE id = ?",
                Integer.class,
                id);
        if (count == null || count == 0) {
            throw new NotFoundException(entityName + " с таким id не найден: " + id);
        }
    }
}
