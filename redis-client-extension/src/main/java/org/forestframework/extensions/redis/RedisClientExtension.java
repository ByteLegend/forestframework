package org.forestframework.extensions.redis;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import org.forestframework.config.ConfigProvider;

public class RedisClientExtension extends AbstractModule {
    private final RedisOptions redisOptions;
    private final Vertx vertx;

    @Inject
    public RedisClientExtension(
            ConfigProvider configProvider,
//            @Named("forest.redis") RedisOptions redisOptions,
            Vertx vertx) {
        this.redisOptions = configProvider.getInstance("forest.redis", RedisOptions.class);
        this.vertx = vertx;
    }

    @Provides
    public RedisAPI createRedisClient() {
        return RedisAPI.api(Redis.createClient(vertx, redisOptions));
    }
}
