package io.forestframework.example.todo.java.sync.redis;

import io.forestframework.example.todo.java.Todo;
import io.forestframework.example.todo.java.sync.TodoService;
import io.vertx.core.json.Json;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Singleton
public class BlockingRedisTodoService implements TodoService {
    private static final String REDIS_TODO_KEY = "VERT_TODO";
    private final JedisPool pool;

    public BlockingRedisTodoService(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public Todo insert(Todo todo) {
        try (Jedis jedis = pool.getResource()) {
            String encoded = Json.encodePrettily(todo);
            jedis.hset(REDIS_TODO_KEY, "" + todo.getId(), encoded);
            return todo;
        }
    }

    @Override
    public List<Todo> all() {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hvals(REDIS_TODO_KEY).stream().map(Todo::new).collect(toList());
        }
    }

    @Override
    public Optional<Todo> getCertain(String todoId) {
        try (Jedis jedis = pool.getResource()) {
            return Optional.ofNullable(jedis.hget(REDIS_TODO_KEY, todoId)).map(Todo::new);
        }
    }

    @Override
    public Optional<Todo> update(String todoId, Todo newTodo) {
        return getCertain(todoId).map(it -> it.merge(newTodo)).map(this::insert);
    }

    @Override
    public void delete(String todoId) {
        try (Jedis jedis = pool.getResource()) {
            jedis.hdel(REDIS_TODO_KEY, todoId);
        }
    }

    @Override
    public void deleteAll() {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(REDIS_TODO_KEY);
        }
    }
}
