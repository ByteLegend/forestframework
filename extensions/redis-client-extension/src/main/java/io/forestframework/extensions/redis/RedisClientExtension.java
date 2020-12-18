package io.forestframework.extensions.redis;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.forestframework.core.config.Config;
import io.forestframework.ext.api.ApplicationContext;
import io.forestframework.ext.api.Extension;
import io.forestframework.utils.completablefuture.VertxCompletableFuture;
import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.RedisOptions;

public class RedisClientExtension implements Extension {
    @Override
    public void start(ApplicationContext applicationContext) {
        applicationContext.getConfigProvider().addDefaultOptions("forest.redis", RedisOptions::new);
        applicationContext.getModules().add(new RedisClientModule());
    }

    @Override
    public void configure(Injector injector) {
        injector.getInstance(RedisAPI.class);
    }

    public static class RedisClientModule extends AbstractModule {
        @Provides
        @Singleton
        public RedisAPI createRedisClient(@Config("forest.redis") RedisOptions redisOptions, Vertx vertx) {
            VertxCompletableFuture<RedisConnection> future = VertxCompletableFuture.from(vertx.getOrCreateContext(), Redis.createClient(vertx, redisOptions).connect());
            try {
                return RedisAPI.api(future.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
