package io.forestframework.ext.pg;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.forestframework.core.config.Config;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.ApplicationContext;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

import javax.inject.Inject;

public class PgClientExtension implements Extension {
    @Override
    public void start(ApplicationContext applicationContext) {
        applicationContext.getComponents().add(PgClientModule.class);
        applicationContext.getConfigProvider().addDefaultOptions("forest.pg.connect", PgConnectOptions::new);
        applicationContext.getConfigProvider().addDefaultOptions("forest.pg.pool", PoolOptions::new);
    }

    public static class PgClientModule extends AbstractModule {
        @Inject
        private Vertx vertx;

        @Inject
        @Config("forest.pg.connect")
        private PgConnectOptions pgConnectOptions;

        @Inject
        @Config("forest.pg.pool")
        private PoolOptions poolOptions;

        @Provides
        public PgPool createClient() {
            return PgPool.pool(vertx, pgConnectOptions, poolOptions);
        }
    }
}
