package org.forestframework.bootstrap;

import com.google.inject.Injector;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.forestframework.http.DefaultHttpVerticle;

import javax.inject.Inject;

public class DefaultHttpServerStarter implements HttpServerStarter {
    private final Vertx vertx;
    private final Injector injector;

    @Inject
    public DefaultHttpServerStarter(Vertx vertx, Injector injector) {
        this.vertx = vertx;
        this.injector = injector;
    }

    @Override
    public void start() {
        vertx.deployVerticle(injector.getInstance(DefaultHttpVerticle.class));
    }
}
