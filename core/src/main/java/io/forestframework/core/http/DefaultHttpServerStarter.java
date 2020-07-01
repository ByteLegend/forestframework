package io.forestframework.core.http;

import com.google.inject.Injector;
import io.forestframework.core.config.Config;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
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
        vertx.deployVerticle(() -> injector.getInstance(DefaultHttpVerticle.class), deploymentOptions);
    }
}
