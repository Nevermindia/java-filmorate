package ru.yandex.practicum.filmorate.storage.mpaStorage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Mpa> mpaRowMapper = new MpaRowMapper();

    @Override
    public List<Mpa> getMpaRatings() {
        return jdbcTemplate.query("SELECT id, name FROM mpa ORDER BY id", mpaRowMapper);
    }

    @Override
    public Mpa getMpaById(Integer id) {
        List<Mpa> ratings = jdbcTemplate.query(
                "SELECT id, name FROM mpa WHERE id = ?",
                mpaRowMapper,
                id);
        return ratings.isEmpty() ? null : ratings.getFirst();
    }
}
