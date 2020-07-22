package io.forestframework.example.todo.java.async.jdbc;

import io.forestframework.example.todo.java.Todo;
import io.forestframework.example.todo.java.async.TodoService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

public class JDBCTodoService implements TodoService {
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
    private final JDBCClient jdbcClient;

    @Inject
    public JDBCTodoService(JDBCClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Future<Void> initData() {
        Promise<Void> promise = Promise.promise();
        jdbcClient.update(SQL_CREATE, handler(promise, result -> null));
        return promise.future();
    }

    @Override
    public Future<Todo> insert(Todo todo) {
        Promise<Todo> promise = Promise.promise();
        JsonArray params = new JsonArray().add(todo.getId())
                .add(todo.getTitle())
                .add(todo.isCompleted())
                .add(todo.getOrder())
                .add(todo.getUrl());

        jdbcClient.updateWithParams(SQL_INSERT, params, handler(promise, result -> todo));
        return promise.future();
    }

    private <RESULT, T> Handler<AsyncResult<RESULT>> handler(Promise<T> promise, Function<RESULT, T> mapper) {
        return result -> {
            if (result.succeeded()) {
                promise.complete(mapper.apply(result.result()));
            } else {
                promise.fail(result.cause());
            }
        };
    }

    @Override
    public Future<List<Todo>> all() {
        Promise<List<Todo>> promise = Promise.promise();
        jdbcClient.query(SQL_QUERY_ALL,
                handler(promise, result -> result.getRows().stream().map(Todo::new).collect(toList())));
        return promise.future();
    }

    @Override
    public Future<Optional<Todo>> getCertain(String todoId) {
        Promise<Optional<Todo>> promise = Promise.promise();
        jdbcClient.queryWithParams(SQL_QUERY, new JsonArray().add(todoId),
                handler(promise, resultSet ->
                        Optional.ofNullable(firstOrNull(resultSet.getRows()))
                                .map(Todo::new)
                )
        );
        return promise.future();
    }

    private <T> T firstOrNull(List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Future<Optional<Todo>> update(String todoId, Todo newTodo) {
        return getCertain(todoId).compose(optional -> {
            if (optional.isPresent()) {
                Todo todo = optional.get().merge(newTodo);
                JsonArray params = new JsonArray()
                        .add(todoId)
                        .add(todo.getTitle())
                        .add(todo.isCompleted())
                        .add(todo.getOrder())
                        .add(todo.getUrl())
                        .add(todoId);
                Promise<Optional<Todo>> promise = Promise.promise();
                jdbcClient.updateWithParams(SQL_UPDATE, params, handler(promise, result -> Optional.of(todo)));
                return promise.future();
            } else {
                return Future.succeededFuture(Optional.empty());
            }
        });
    }

    @Override
    public Future<Void> delete(String todoId) {
        Promise<Void> promise = Promise.promise();
        jdbcClient.updateWithParams(SQL_DELETE, new JsonArray().add(todoId), handler(promise, result -> null));
        return promise.future();
    }

    @Override
    public Future<Void> deleteAll() {
        Promise<Void> promise = Promise.promise();
        jdbcClient.update(SQL_DELETE_ALL, handler(promise, result -> null));
        return promise.future();
    }
}
