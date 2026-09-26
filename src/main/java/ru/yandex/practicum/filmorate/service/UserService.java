package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.userStorage.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage storage;

    public User createUser(User user) {
        return storage.createUser(user);
    }

    public User updateUser(User user) {
        getUserById(user.getId());
        return storage.updateUser(user);
    }

    public List<User> getUsers() {
        return storage.getUsers();
    }

    public User getUserById(Long id) {
        User userById = storage.getUserById(id);
        if (userById == null) {
            throw new NotFoundException("Пользователя с таким id не существует: " + id);
        }
        return userById;
    }

    public void addToFriend(Long id, Long friendId) {
        User user = getUserById(id);
        User friend = getUserById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(id);
        log.info("User {} added to friends user {}", id, friendId);
    }

    public List<User> getFriends(Long id) {
        return getUserById(id).getFriends().stream()
                .map(this::getUserById)
                .toList();
    }

    public void removeFromFriends(Long id, Long friendId) {
        getUserById(id).getFriends().remove(friendId);
        getUserById(friendId).getFriends().remove(id);
        log.info("User {} removed from friends user {}", id, friendId);

    }

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
