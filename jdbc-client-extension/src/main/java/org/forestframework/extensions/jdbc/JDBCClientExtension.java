package org.forestframework.extensions.jdbc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import org.forestframework.config.ConfigProvider;

import javax.inject.Inject;
import javax.inject.Named;

public class JDBCClientExtension extends AbstractModule {
    private JsonObject config;
    private Vertx vertx;

    @Inject
    public JDBCClientExtension(
//            @Named("forest.jdbc") JsonObject config,
            ConfigProvider configProvider,
            Vertx vertx) {
        this.config = configProvider.getInstance("forest.jdbc", JsonObject.class);
        this.vertx = vertx;
    }

    @Provides
    public JDBCClient createClient() {
        return JDBCClient.create(vertx, config);
    }
}
