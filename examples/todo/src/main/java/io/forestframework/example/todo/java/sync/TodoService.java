package io.forestframework.example.todo.java.sync;

import io.forestframework.example.todo.java.Todo;

import java.util.List;
import java.util.Optional;

public interface TodoService {
    default void initData() {
    }

    Todo insert(Todo todo);

    List<Todo> all();

    Optional<Todo> getCertain(String todoId);

    Optional<Todo> update(String todoId, Todo newTodo);

    void delete(String todoId);

    void deleteAll();
}
