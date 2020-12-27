package io.forestframework.example.todo.java.sync.jdbc;

import io.forestframework.core.config.ConfigProvider;
import io.forestframework.example.todo.java.Todo;
import io.forestframework.example.todo.java.sync.TodoService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class BlockingJDBCTodoService implements TodoService {
    private static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS `todo` (\n" +
            "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
            "  `title` varchar(255) DEFAULT NULL,\n" +
            "  `completed` tinyint(1) DEFAULT NULL,\n" +
            "  `order` int(11) DEFAULT NULL,\n" +
            "  `url` varchar(255) DEFAULT NULL,\n" +
            "  PRIMARY KEY (`id`) )";
    private static final String SQL_INSERT = "INSERT INTO `todo` " +
            "(`id`, `title`, `completed`, `order`, `url`) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE `todo`\n" +
            "        SET\n" +
            "        `id` = ?,\n" +
            "        `title` = ?,\n" +
            "        `completed` = ?,\n" +
            "        `order` = ?,\n" +
            "        `url` = ?\n" +
            "        WHERE `id` = ?";
    private static final String SQL_QUERY_ALL = "SELECT `id`, `title`, `completed`, `order`, `url` FROM todo";
    private static final String SQL_DELETE = "DELETE FROM `todo` WHERE `id` = ?";
    private static final String SQL_DELETE_ALL = "DELETE FROM `todo`";
    private static final String SQL_QUERY = "SELECT `id`, `title`, `completed`, `order`, `url` FROM todo WHERE id = ?";

    private final String databaseUrl;
    private final String databaseUsername;
    private final String databasePassword;


    @Inject
    public BlockingJDBCTodoService(ConfigProvider configProvider) {
        this.databaseUrl = configProvider.getInstance("jdbc.url", String.class);
        this.databaseUsername = configProvider.getInstance("jdbc.root", String.class);
        this.databasePassword = configProvider.getInstance("jdbc.root", String.class);

    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(databaseUrl, databaseUsername, databaseUrl);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initData() {
        executeUpdate(SQL_CREATE);
    }

    private void executeUpdate(String sql) {
        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Todo insert(Todo todo) {
        try (PreparedStatement ps = getConnection().prepareStatement(SQL_INSERT)) {
            ps.setInt(1, todo.getId());
            ps.setString(2, todo.getTitle());
            ps.setBoolean(3, todo.isCompleted());
            ps.setInt(4, todo.getOrder());
            ps.setString(5, todo.getUrl());
            ps.executeUpdate();
            return todo;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Todo> all() {
        try (Statement statement = getConnection().createStatement()) {
            List<Todo> todos = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery(SQL_QUERY_ALL);
            while (resultSet.next()) {
                todos.add(new Todo(
                        resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getBoolean(3),
                        resultSet.getInt(4),
                        resultSet.getString(5))
                );
            }

            return todos;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Todo> getCertain(String todoId) {
        try (PreparedStatement ps = getConnection().prepareStatement(SQL_QUERY)) {
            ps.setString(1, todoId);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return Optional.of(new Todo(
                        resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getBoolean(3),
                        resultSet.getInt(4),
                        resultSet.getString(5))
                );
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Todo> update(String todoId, Todo newTodo) {
        return getCertain(todoId).map(oldTodo -> {
            Todo todo = oldTodo.merge(newTodo);
            try (PreparedStatement ps = getConnection().prepareStatement(SQL_UPDATE)) {
                ps.setInt(1, todo.getId());
                ps.setString(2, todo.getTitle());
                ps.setBoolean(3, todo.isCompleted());
                ps.setInt(4, todo.getOrder());
                ps.setString(5, todo.getUrl());
                ps.setString(6, todoId);
                ps.executeUpdate();
                return todo;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void delete(String todoId) {
        try (PreparedStatement ps = getConnection().prepareStatement(SQL_DELETE)) {
            ps.setString(1, todoId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAll() {
        executeUpdate(SQL_DELETE_ALL);
    }
}
