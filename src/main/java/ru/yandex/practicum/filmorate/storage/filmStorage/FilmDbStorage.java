package ru.yandex.practicum.filmorate.storage.filmStorage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String SELECT_FILMS = "SELECT f.id, f.name, f.description, f.release_date, "
            + "f.duration, f.mpa_id, m.name AS mpa_name FROM films f "
            + "LEFT JOIN mpa m ON f.mpa_id = m.id";

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Film> filmRowMapper = this::mapFilm;
    private final RowMapper<Genre> genreRowMapper = new GenreRowMapper();

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
        loadRelations(films);
        return films;
    }

    @Override
    public Film getFilmById(Long id) {
        List<Film> films = jdbcTemplate.query(SELECT_FILMS + " WHERE f.id = ?", filmRowMapper, id);
        if (films.isEmpty()) {
            return null;
        }
        Film film = films.getFirst();
        loadRelations(films);
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
        loadRelations(films);
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

    private void loadRelations(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        Map<Long, Film> filmsById = new HashMap<>();
        films.forEach(film -> {
            film.setGenres(new LinkedHashSet<>());
            film.setLikes(new HashSet<>());
            filmsById.put(film.getId(), film);
        });
        List<Long> filmIds = films.stream().map(Film::getId).toList();
        String placeholders = String.join(", ", Collections.nCopies(filmIds.size(), "?"));

        String genreSql = "SELECT fg.film_id, g.id, g.name FROM genres g "
                + "JOIN film_genres fg ON g.id = fg.genre_id "
                + "WHERE fg.film_id IN (" + placeholders + ") ORDER BY fg.film_id, g.id";
        List<FilmGenre> genres = jdbcTemplate.query(genreSql,
                (resultSet, rowNum) -> new FilmGenre(
                        resultSet.getLong("film_id"),
                        genreRowMapper.mapRow(resultSet, rowNum)),
                filmIds.toArray());
        genres.forEach(relation -> filmsById.get(relation.filmId()).getGenres().add(relation.genre()));

        String likesSql = "SELECT film_id, user_id FROM film_likes "
                + "WHERE film_id IN (" + placeholders + ") ORDER BY film_id, user_id";
        List<FilmLike> likes = jdbcTemplate.query(likesSql,
                (resultSet, rowNum) -> new FilmLike(
                        resultSet.getLong("film_id"),
                        resultSet.getLong("user_id")),
                filmIds.toArray());
        likes.forEach(relation -> filmsById.get(relation.filmId()).getLikes().add(relation.userId()));
    }

    private void saveGenres(Long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        List<Integer> genreIds = genres.stream().map(Genre::getId).distinct().toList();
        jdbcTemplate.batchUpdate(
                "MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) VALUES (?, ?)",
                genreIds,
                genreIds.size(),
                (statement, genreId) -> {
                    statement.setLong(1, filmId);
                    statement.setInt(2, genreId);
                });
    }

    private void validateReferences(Film film) {
        if (film.getMpa() != null) {
            ensureReferenceExists("mpa", film.getMpa().getId(), "Рейтинг MPA");
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            validateGenres(film.getGenres());
        }
    }

    private void validateGenres(Set<Genre> genres) {
        List<Integer> genreIds = genres.stream().map(Genre::getId).distinct().toList();
        String placeholders = String.join(", ", Collections.nCopies(genreIds.size(), "?"));
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM genres WHERE id IN (" + placeholders + ")",
                Integer.class,
                genreIds.toArray());
        if (count == null || count != genreIds.size()) {
            throw new NotFoundException("Один или несколько жанров не найдены");
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

    private record FilmGenre(Long filmId, Genre genre) {
    }

    private record FilmLike(Long filmId, Long userId) {
    }
}
