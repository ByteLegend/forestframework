package org.forestframework.extensions.redis;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;
import org.forestframework.annotation.Config;

public class RedisClientExtension extends AbstractModule {
    private final RedisOptions redisOptions;
    private final Vertx vertx;

    @Inject
    public RedisClientExtension(
            @Config("forest.redis") RedisOptions redisOptions,
            Vertx vertx) {
        this.redisOptions = redisOptions;
        this.vertx = vertx;
    }

    @Provides
    public Redis createRedisClient() {
        return Redis.createClient(vertx, redisOptions);
    }
}
