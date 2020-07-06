package io.forestframework.core.http;

import com.google.inject.Injector;
import io.forestframework.core.config.Config;
import io.forestframework.core.http.routing.DefaultRoutings;
import io.forestframework.core.http.routing.Routings;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultHttpServerStarter implements HttpServerStarter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpServerStarter.class);
    private final Vertx vertx;
    private final Injector injector;
    private final DeploymentOptions deploymentOptions;
    private final Routings routings;

    @Inject
    public DefaultHttpServerStarter(Vertx vertx,
                                    Injector injector,
                                    @Config("forest.deploy") DeploymentOptions deploymentOptions,
                                    Routings routings) {
        this.vertx = vertx;
        this.injector = injector;
        this.deploymentOptions = deploymentOptions;
        this.routings = routings;
    }

    @Override
    public void start() {
        ((DefaultRoutings) routings).finalizeRoutings();
        vertx.deployVerticle(() -> injector.getInstance(DefaultHttpVerticle.class), deploymentOptions)
                .onFailure((e) -> LOGGER.error("", e));
    }
}
