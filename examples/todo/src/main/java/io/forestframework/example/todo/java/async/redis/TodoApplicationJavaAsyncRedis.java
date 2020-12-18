package io.forestframework.example.todo.java.async.redis;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.forestframework.core.Forest;
import io.forestframework.core.ForestApplication;
import io.forestframework.example.todo.java.async.TodoRouter;
import io.forestframework.example.todo.java.async.TodoService;
import io.forestframework.ext.core.IncludeComponents;
import io.forestframework.ext.core.WithStaticResource;
import io.forestframework.extensions.redis.EnableRedisClient;
import io.vertx.redis.client.RedisAPI;

import static io.forestframework.example.todo.java.async.redis.TodoApplicationJavaAsyncRedis.RedisModule;

@WithStaticResource
@ForestApplication
@IncludeComponents(classes = {RedisModule.class, TodoRouter.class})
@EnableRedisClient
public class TodoApplicationJavaAsyncRedis {
    public static void main(String[] args) {
        Forest.run(TodoApplicationJavaAsyncRedis.class);
    }

    public static class RedisModule extends AbstractModule {
        @Provides
        @Singleton
        public TodoService todoService(RedisAPI client) {
            return new RedisTodoService(client);
        }
    }
}
