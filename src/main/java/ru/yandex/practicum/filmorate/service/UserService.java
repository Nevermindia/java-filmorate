package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.userStorage.UserStorage;

import java.util.List;

@Service
public class UserService {

    private final UserStorage storage;

    public UserService(@Qualifier("userDbStorage") UserStorage storage) {
        this.storage = storage;
    }

    public User createUser(User user) {
        resolveName(user);
        return storage.createUser(user);
    }

    public User updateUser(User user) {
        getUserById(user.getId());
        resolveName(user);
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
        getUserById(id);
        getUserById(friendId);
        if (id.equals(friendId)) {
            throw new ValidationException("Нельзя добавить в друзья самого себя");
        }
        storage.addToFriend(id, friendId);
    }

    public List<User> getFriends(Long id) {
        getUserById(id);
        return storage.getFriends(id);
    }

    public void removeFromFriends(Long id, Long friendId) {
        getUserById(id);
        getUserById(friendId);
        storage.removeFromFriends(id, friendId);

    }

    public List<User> getFriendsInCommon(Long id, Long otherId) {
        getUserById(id);
        getUserById(otherId);
        return storage.getFriendsInCommon(id, otherId);
    }

    private void resolveName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
