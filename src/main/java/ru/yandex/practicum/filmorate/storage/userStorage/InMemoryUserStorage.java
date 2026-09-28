package ru.yandex.practicum.filmorate.storage.userStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public User createUser(User user) {

        Long id = nextId++;
        user.setId(id);
        users.put(id, user);

        log.info("User created successfully with id: {}", user.getId());

        return user;
    }

    @Override
    public User updateUser(User user) {

        Long id = user.getId();
        User existingUser = users.get(id);
        if (existingUser == null) {
            throw new NotFoundException("Пользователя с таким id не существует: " + id);
        }

        log.debug("Found existing user: {}", existingUser.getId());

        existingUser.setEmail(user.getEmail());
        existingUser.setLogin(user.getLogin());
        existingUser.setName(user.getName());
        existingUser.setBirthday(user.getBirthday());

        log.info("User updated successfully with id: {}", existingUser.getId());

        return existingUser;
    }

    @Override
    public List<User> getUsers() {
        log.debug("Returning all users, count: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long id) {
        log.debug("Getting user with id: {}", id);
        return users.get(id);
    }
}
