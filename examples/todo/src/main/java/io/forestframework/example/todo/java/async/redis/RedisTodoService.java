package io.forestframework.example.todo.java.async.redis;

import io.forestframework.example.todo.java.Todo;
import io.forestframework.example.todo.java.async.TodoService;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class RedisTodoService implements TodoService {
    private static final String REDIS_TODO_KEY = "VERT_TODO";
    private final RedisAPI client;

    @Inject
    public RedisTodoService(RedisAPI redisAPI) {
        this.client = redisAPI;
    }

    @Override
    public Future<Todo> insert(Todo todo) {
        String encoded = Json.encodePrettily(todo);
        return client.hset(Arrays.asList(REDIS_TODO_KEY, "" + todo.getId(), encoded)).map(r -> todo);
    }

    @Override
    public Future<List<Todo>> all() {
        return client.hvals(REDIS_TODO_KEY).map(response -> {
            if (response != null) {
                return response.stream().map(it -> new Todo(it.toString())).collect(toList());
            } else {
                return emptyList();
            }
        });
    }

    @Override
    public Future<Optional<Todo>> getCertain(String todoId) {
        return client.hget(REDIS_TODO_KEY, todoId)
                .map(response -> Optional.ofNullable(response).map(Response::toString).map(Todo::new));
    }

    @Override
    public Future<Optional<Todo>> update(String todoId, Todo newTodo) {
        return getCertain(todoId).compose((Optional<Todo> oldTodo) -> {
            if (oldTodo.isPresent()) {
                Todo result = oldTodo.get().merge(newTodo);
                return insert(result).map(Optional::of);
            } else {
                return Future.succeededFuture(Optional.empty());
            }
        });
    }

    @Override
    public Future<Void> delete(String todoId) {
        return client.hdel(Arrays.asList(REDIS_TODO_KEY, todoId)).map(r -> null);
    }

    @Override
    public Future<Void> deleteAll() {
        return client.del(singletonList(REDIS_TODO_KEY)).map(r -> null);
    }
}
