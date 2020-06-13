package io.forestframework.ext.pgclient;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.reactiverse.pgclient.PgClient;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.forestframework.config.ConfigProvider;

@Singleton
public class PgClientExtension extends AbstractModule {
    private final Vertx vertx;
    private final PgConnectOptions pgConnectOptions;
    private final PoolOptions poolOptions;

    @Inject
    public PgClientExtension(Vertx vertx,
                             ConfigProvider configProvider
//                             PgConnectOptions pgConnectOptions, PoolOptions poolOptions
    ) {
        this.vertx = vertx;
        this.pgConnectOptions = configProvider.getInstance("forest.pg.connect", PgConnectOptions.class);
        this.poolOptions = configProvider.getInstance("forest.pg.pool", PoolOptions.class);
    }

    @Provides
    public PgPool createClient() {
        return PgPool.pool(vertx, pgConnectOptions, poolOptions);
    }

    @Provides
    public PgClient createPgClient() {
        return null;
    }
}
