package io.forestframework.ext.pg;

import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.ExtensionContext;
import io.vertx.pgclient.PgConnectOptions;

public class PgClientExtension implements Extension {
    @Override
    public void beforeInjector(ExtensionContext extensionContext) {
        extensionContext.getComponentClasses().add(PgClientModule.class);
        extensionContext.getConfigProvider().addDefaultOptions("forest.pg.connect", PgConnectOptions.class);
        extensionContext.getConfigProvider().addDefaultOptions("forest.pg.pool", PoolOptions.class);
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
