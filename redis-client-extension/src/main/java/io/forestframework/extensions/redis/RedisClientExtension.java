package io.forestframework.extensions.redis;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.forestframework.core.config.Config;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;
import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.RedisOptions;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

public class RedisClientExtension implements Extension {
    @Override
    public void beforeInjector(StartupContext startupContext) {
        startupContext.getConfigProvider().addDefaultOptions("forest.redis", RedisOptions.class);
        startupContext.getComponentClasses().add(RedisClientModule.class);
    }

    @Override
    public void afterInjector(Injector injector) {
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
