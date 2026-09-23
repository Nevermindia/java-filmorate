package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final ArrayList<User> users = new ArrayList<>();
    private Integer nextId = 1;

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.debug("Received user data: {}", user);

        resolveName(user);

        user.setId(nextId++);
        users.add(user);

        log.info("User created successfully with id: {}", user.getId());
        log.debug("Created user: {}", user);

        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.debug("Received update request for user: {}", user);

        Integer id = user.getId();
        log.debug("Looking for user with id: {}", id);

        User existingUser = users.stream()
                .filter(u -> u.getId() == id)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new NotFoundException("User not found with id: " + id);
                });

        log.debug("Found existing user: {}", existingUser);

        resolveName(user);

        log.debug("Updating email from '{}' to '{}'", existingUser.getEmail(), user.getEmail());
        existingUser.setEmail(user.getEmail());

        log.debug("Updating login from '{}' to '{}'", existingUser.getLogin(), user.getLogin());
        existingUser.setLogin(user.getLogin());

        log.debug("Updating name from '{}' to '{}'", existingUser.getName(), user.getName());
        existingUser.setName(user.getName());

        log.debug("Updating birthday from '{}' to '{}'", existingUser.getBirthday(), user.getBirthday());
        existingUser.setBirthday(user.getBirthday());

        log.info("User updated successfully with id: {}", existingUser.getId());
        log.debug("Updated user: {}", existingUser);

        return existingUser;
    }

    @GetMapping
    public ArrayList<User> getUsers() {
        log.info("Returning all users, count: {}", users.size());
        log.debug("Users list: {}", users);
        return users;
    }

    private void resolveName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Name is empty, using login as name: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}