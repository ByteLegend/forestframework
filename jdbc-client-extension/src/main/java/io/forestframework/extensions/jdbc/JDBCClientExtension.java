package io.forestframework.extensions.jdbc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.forestframework.core.config.Config;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import java.util.List;

public class JDBCClientExtension extends AbstractModule {
    @Provides
    public JDBCClient createClient(@Config("forest.jdbc") JsonObject config, Vertx vertx) {
        return JDBCClient.create(vertx, config);
    }

    public void configure(List<Class<?>> componentClasses) {
        componentClasses.add(getClass());
    }
}
