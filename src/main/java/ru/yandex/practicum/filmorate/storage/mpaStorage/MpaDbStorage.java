package ru.yandex.practicum.filmorate.storage.mpaStorage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> getMpaRatings() {
        return jdbcTemplate.query("SELECT id, name FROM mpa ORDER BY id", (resultSet, rowNum) -> {
            Mpa mpa = new Mpa();
            mpa.setId(resultSet.getInt("id"));
            mpa.setName(resultSet.getString("name"));
            return mpa;
        });
    }

    @Override
    public Mpa getMpaById(Integer id) {
        List<Mpa> ratings = jdbcTemplate.query(
                "SELECT id, name FROM mpa WHERE id = ?",
                (resultSet, rowNum) -> {
                    Mpa mpa = new Mpa();
                    mpa.setId(resultSet.getInt("id"));
                    mpa.setName(resultSet.getString("name"));
                    return mpa;
                },
                id);
        return ratings.isEmpty() ? null : ratings.getFirst();
    }
}
