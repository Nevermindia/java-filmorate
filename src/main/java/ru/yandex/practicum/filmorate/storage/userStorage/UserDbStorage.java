package ru.yandex.practicum.filmorate.storage.userStorage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Primary
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private static final String SELECT_USERS = "SELECT id, email, login, name, birthday FROM users";

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = this::mapUser;

    @Override
    public User createUser(User user) {
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
    }

    @Override
    public User updateUser(User user) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        if (updatedRows == 0) {
            throw new NotFoundException("Пользователя с таким id не существует: " + user.getId());
        }
        return getUserById(user.getId());
    }

    @Override
    public List<User> getUsers() {
        List<User> users = jdbcTemplate.query(SELECT_USERS + " ORDER BY id", userRowMapper);
        users.forEach(this::loadFriends);
        return users;
    }

    @Override
    public User getUserById(Long id) {
        List<User> users = jdbcTemplate.query(SELECT_USERS + " WHERE id = ?", userRowMapper, id);
        if (users.isEmpty()) {
            return null;
        }
        User user = users.getFirst();
        loadFriends(user);
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
        friends.forEach(this::loadFriends);
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
        commonFriends.forEach(this::loadFriends);
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

    private void loadFriends(User user) {
        List<Long> friendIds = jdbcTemplate.queryForList(
                "SELECT friend_id FROM friendships WHERE user_id = ? ORDER BY friend_id",
                Long.class,
                user.getId());
        user.setFriends(new HashSet<>(friendIds));
    }
}
