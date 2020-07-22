package io.forestframework.example.todo.java.async;

import io.forestframework.example.todo.java.Todo;
import io.vertx.core.Future;

import java.util.List;
import java.util.Optional;

public interface TodoService {
    default Future<Void> initData() {
        return Future.succeededFuture(null);
    }

    Future<Todo> insert(Todo todo);

    Future<List<Todo>> all();

    Future<Optional<Todo>> getCertain(String todoId);

    Future<Optional<Todo>> update(String todoId, Todo newTodo);

    Future<Void> delete(String todoId);

    Future<Void> deleteAll();
}
