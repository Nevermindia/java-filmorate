package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpaStorage.MpaStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage storage;

    public List<Mpa> getMpaRatings() {
        return storage.getMpaRatings();
    }

    public Mpa getMpaById(Integer id) {
        Mpa mpa = storage.getMpaById(id);
        if (mpa == null) {
            throw new NotFoundException("Рейтинг MPA с таким id не найден: " + id);
        }
        return mpa;
    }
}
