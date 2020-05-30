package org.forestframework.extensions.jdbc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import javax.inject.Named;

public class JDBCClientExtension extends AbstractModule {
    private JsonObject config;
    private Vertx vertx;

    public JDBCClientExtension(@Named("forest.jdbc") JsonObject config,
                               Vertx vertx) {
        this.config = config;
        this.vertx = vertx;
    }

    @Provides
    public JDBCClient createClient() {
        return JDBCClient.create(vertx, config);
    }
}
