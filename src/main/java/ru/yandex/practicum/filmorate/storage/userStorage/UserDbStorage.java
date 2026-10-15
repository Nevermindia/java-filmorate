package ru.yandex.practicum.filmorate.storage.userStorage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private static final String SELECT_USERS = "SELECT id, email, login, name, birthday FROM users";

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = this::mapUser;

    @Override
    public User createUser(User user) {
        try {
            Map<String, Object> values = new HashMap<>();
            values.put("email", user.getEmail());
            values.put("login", user.getLogin());
            values.put("name", user.getName());
            values.put("birthday", user.getBirthday());

            Number id = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("users")
                    .usingGeneratedKeyColumns("id")
                    .executeAndReturnKey(values);
            return getUserById(id.longValue());
        } catch (DuplicateKeyException e) {
            throw duplicateUserException();
        }
    }

    @Override
    public User updateUser(User user) {
        int updatedRows;
        try {
            updatedRows = jdbcTemplate.update(
                    "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?",
                    user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        } catch (DuplicateKeyException e) {
            throw duplicateUserException();
        }
        if (updatedRows == 0) {
            throw new NotFoundException("Пользователя с таким id не существует: " + user.getId());
        }
        return getUserById(user.getId());
    }

    @Override
    public List<User> getUsers() {
        List<User> users = jdbcTemplate.query(SELECT_USERS + " ORDER BY id", userRowMapper);
        loadFriends(users);
        return users;
    }

    @Override
    public User getUserById(Long id) {
        List<User> users = jdbcTemplate.query(SELECT_USERS + " WHERE id = ?", userRowMapper, id);
        if (users.isEmpty()) {
            return null;
        }
        User user = users.getFirst();
        loadFriends(users);
        return user;
    }

    @Override
    public void addToFriend(Long id, Long friendId) {
        jdbcTemplate.update(
                "MERGE INTO friendships (user_id, friend_id) KEY (user_id, friend_id) VALUES (?, ?)",
                id, friendId);
    }

    @Override
    public List<User> getFriends(Long id) {
        String sql = SELECT_USERS
                + " JOIN friendships f ON users.id = f.friend_id"
                + " WHERE f.user_id = ? ORDER BY users.id";
        List<User> friends = jdbcTemplate.query(sql, userRowMapper, id);
        loadFriends(friends);
        return friends;
    }

    @Override
    public void removeFromFriends(Long id, Long friendId) {
        jdbcTemplate.update("DELETE FROM friendships WHERE user_id = ? AND friend_id = ?", id, friendId);
    }

    @Override
    public List<User> getFriendsInCommon(Long id, Long otherId) {
        String sql = SELECT_USERS
                + " JOIN friendships first_friend ON users.id = first_friend.friend_id"
                + " JOIN friendships second_friend ON users.id = second_friend.friend_id"
                + " WHERE first_friend.user_id = ? AND second_friend.user_id = ?"
                + " ORDER BY users.id";
        List<User> commonFriends = jdbcTemplate.query(sql, userRowMapper, id, otherId);
        loadFriends(commonFriends);
        return commonFriends;
    }

    private User mapUser(ResultSet resultSet, int rowNum) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setEmail(resultSet.getString("email"));
        user.setLogin(resultSet.getString("login"));
        user.setName(resultSet.getString("name"));
        user.setBirthday(resultSet.getDate("birthday").toLocalDate());
        return user;
    }

    private void loadFriends(List<User> users) {
        if (users.isEmpty()) {
            return;
        }
        Map<Long, User> usersById = new HashMap<>();
        users.forEach(user -> {
            user.setFriends(new HashSet<>());
            usersById.put(user.getId(), user);
        });
        List<Long> userIds = users.stream().map(User::getId).toList();
        String placeholders = String.join(", ", Collections.nCopies(userIds.size(), "?"));
        String sql = "SELECT user_id, friend_id FROM friendships "
                + "WHERE user_id IN (" + placeholders + ") ORDER BY user_id, friend_id";
        List<Friendship> friendships = jdbcTemplate.query(sql,
                (resultSet, rowNum) -> new Friendship(
                        resultSet.getLong("user_id"),
                        resultSet.getLong("friend_id")),
                userIds.toArray());
        friendships.forEach(friendship ->
                usersById.get(friendship.userId()).getFriends().add(friendship.friendId()));
    }

    private record Friendship(Long userId, Long friendId) {
    }

    private ValidationException duplicateUserException() {
        return new ValidationException("Пользователь с таким email или логином уже существует");
    }
}
