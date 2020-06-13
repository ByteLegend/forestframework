package io.forestframework.bootstrap;

import com.google.inject.Injector;
import io.forestframework.config.Config;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.forestframework.http.DefaultHttpVerticle;

import javax.inject.Inject;

public class DefaultHttpServerStarter implements HttpServerStarter {
    private final Vertx vertx;
    private final Injector injector;
    private final DeploymentOptions deploymentOptions;

    @Inject
    public DefaultHttpServerStarter(Vertx vertx,
                                    Injector injector,
                                    @Config("forest.deploy") DeploymentOptions deploymentOptions) {
        this.vertx = vertx;
        this.injector = injector;
        this.deploymentOptions = deploymentOptions;
    }

    @Override
    public void start() {
        vertx.deployVerticle(injector.getInstance(DefaultHttpVerticle.class), deploymentOptions);
    }
}
