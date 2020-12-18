package io.forestframework.example.todo.java.sync.redis;


import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.forestframework.core.Forest;
import io.forestframework.core.ForestApplication;
import io.forestframework.core.config.Config;
import io.forestframework.example.todo.java.sync.TodoRouter;
import io.forestframework.example.todo.java.sync.TodoService;
import io.forestframework.ext.core.IncludeComponents;
import io.forestframework.ext.core.WithStaticResource;
import io.forestframework.extensions.redis.EnableRedisClient;
import io.vertx.redis.client.RedisOptions;
import redis.clients.jedis.JedisPool;

import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;

import static io.forestframework.example.todo.java.sync.redis.TodoApplicationJavaSyncRedis.RedisModule;

@WithStaticResource
@IncludeComponents(classes = {TodoRouter.class, RedisModule.class})
@EnableRedisClient
@ForestApplication
public class TodoApplicationJavaSyncRedis {
    public static void main(String[] args) {
        Forest.run(TodoApplicationJavaSyncRedis.class);
    }

    public static class RedisModule extends AbstractModule {
        @Singleton
        @Provides
        public TodoService getTodoService(@Config("forest.redis") RedisOptions redisOptions) throws URISyntaxException {
            JedisPool pool = new JedisPool(new URI(redisOptions.getEndpoint()));
            return new BlockingRedisTodoService(pool);
        }
    }
}
