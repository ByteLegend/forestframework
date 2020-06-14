package io.forestframework.extensions.redis;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.forestframework.config.Config;
import io.forestframework.ext.api.ComponentsConfigurer;
import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;

import java.util.List;

public class RedisClientExtension extends AbstractModule implements ComponentsConfigurer {
    @Provides
    public Redis createRedisClient(@Config("forest.redis") RedisOptions redisOptions, Vertx vertx) {
        return Redis.createClient(vertx, redisOptions);
    }

    @Override
    public void configure(List<Class<?>> componentClasses) {
        componentClasses.add(getClass());
    }
}
