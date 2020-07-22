package io.forestframework.example.todo.java.sync.jdbc;

import io.forestframework.example.todo.java.Todo;
import io.forestframework.example.todo.java.sync.TodoService;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
public class BlockingJDBCTodoService implements TodoService {
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
