package ru.yandex.practicum.filmorate.storage.mpaStorage;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface MpaStorage {
    List<Mpa> getMpaRatings();

    Mpa getMpaById(Integer id);
}
