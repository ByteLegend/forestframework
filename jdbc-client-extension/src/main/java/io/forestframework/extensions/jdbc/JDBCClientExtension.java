package io.forestframework.extensions.jdbc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.forestframework.annotation.Config;

import javax.inject.Inject;

public class JDBCClientExtension extends AbstractModule {
    private JsonObject config;
    private Vertx vertx;

    @Inject
    public JDBCClientExtension(
            @Config("forest.jdbc") JsonObject config,
            Vertx vertx) {
        this.config = config;
        this.vertx = vertx;
    }

    @Provides
    public JDBCClient createClient() {
        return JDBCClient.create(vertx, config);
    }
}
