package ru.yandex.practicum.filmorate.storage.userStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public void addToFriend(Long id, Long friendId) {
        User user = getUserById(id);
        user.getFriends().add(friendId);
        log.info("User {} added to friends user {}", id, friendId);
    }

    @Override
    public List<User> getFriends(Long id) {
        return getUserById(id).getFriends().stream()
                .map(this::getUserById)
                .toList();
    }

    @Override
    public void removeFromFriends(Long id, Long friendId) {
        getUserById(id).getFriends().remove(friendId);
        log.info("User {} removed from friends user {}", id, friendId);
    }

    @Override
    public List<User> getFriendsInCommon(Long id, Long otherId) {
        log.debug("Getting friends in common for users with ids: {} and {}", id, otherId);
        Set<Long> firstUserFriends = getUserById(id).getFriends();
        Set<Long> secondUserFriends = getUserById(otherId).getFriends();
        Set<Long> friendsInCommonIds = firstUserFriends.stream()
                .filter(secondUserFriends::contains)
                .collect(Collectors.toSet());
        return friendsInCommonIds.stream().map(this::getUserById).toList();
    }
}
