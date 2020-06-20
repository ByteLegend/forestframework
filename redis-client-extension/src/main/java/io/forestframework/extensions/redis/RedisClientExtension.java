package io.forestframework.extensions.redis;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.forestframework.config.Config;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.ExtensionContext;
import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;

public class RedisClientExtension implements Extension {
    @Override
    public void beforeInjector(ExtensionContext extensionContext) {
        extensionContext.getConfigProvider().addDefaultOptions("forest.redis", RedisOptions.class);
        extensionContext.getComponentClasses().add(RedisClientModule.class);
    }

    public static class RedisClientModule extends AbstractModule {
        @Provides
        public Redis createRedisClient(@Config("forest.redis") RedisOptions redisOptions, Vertx vertx) {
            return Redis.createClient(vertx, redisOptions);
        }
    }
}
