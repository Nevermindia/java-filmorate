package ru.yandex.practicum.filmorate.storage.userStorage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User createUser(User user);

    User updateUser(User user);

    List<User> getUsers();

    User getUserById(Long id);

    void addToFriend(Long id, Long friendId);

    List<User> getFriends(Long id);

    void removeFromFriends(Long id, Long friendId);

    List<User> getFriendsInCommon(Long id, Long otherId);
}
