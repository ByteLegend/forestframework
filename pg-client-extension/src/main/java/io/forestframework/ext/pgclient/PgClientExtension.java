package io.forestframework.ext.pgclient;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.forestframework.config.Config;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.ExtensionContext;
import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgPoolOptions;
import io.vertx.core.Vertx;

@Singleton
public class PgClientExtension implements Extension {
    @Override
    public void beforeInjector(ExtensionContext extensionContext) {
        extensionContext.getComponentClasses().add(PgClientModule.class);
        extensionContext.getConfigProvider().addDefaultOptions("forest.pg", PgPoolOptions.class);
    }

    public static class PgClientModule extends AbstractModule {
        @Inject
        private Vertx vertx;

        @Inject
        @Config("forest.pg")
        private PgPoolOptions poolOptions;

        @Provides
        public PgPool createClient() {
            return PgClient.pool(vertx, new PgPoolOptions(poolOptions).setMaxSize(4));
        }

        @Provides
        public PgClient createPgClient() {
            return PgClient.pool(vertx, new PgPoolOptions(poolOptions).setMaxSize(1));
        }
    }
}
