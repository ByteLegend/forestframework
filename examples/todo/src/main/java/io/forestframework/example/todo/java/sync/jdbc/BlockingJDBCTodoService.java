package io.forestframework.example.todo.java.sync.jdbc;

import io.forestframework.example.todo.java.Todo;
import io.forestframework.example.todo.java.sync.TodoService;

import javax.inject.Singleton;
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
    private static final String SQL_QUERY_ALL = "SELECT * FROM todo";
    private static final String SQL_DELETE = "DELETE FROM `todo` WHERE `id` = ?";
    private static final String SQL_DELETE_ALL = "DELETE FROM `todo`";
    private static final String SQL_QUERY = "SELECT * FROM todo WHERE id = ?";

    @Override
    public Todo insert(Todo todo) {
        return null;
    }

    @Override
    public List<Todo> all() {
        return null;
    }

    @Override
    public Optional<Todo> getCertain(String todoId) {
        return Optional.empty();
    }

    @Override
    public Optional<Todo> update(String todoId, Todo newTodo) {
        return Optional.empty();
    }

    @Override
    public void delete(String todoId) {

    }

    @Override
    public void deleteAll() {

    }
}
