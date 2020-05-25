package org.forestframework.http;

import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultHttpVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpVerticle.class);
    private final Vertx vertx;
    private final RoutingEngine routingEngine;

    @Inject
    public DefaultHttpVerticle(Vertx vertx, RoutingEngine routingEngine) {
        this.vertx = vertx;
        this.routingEngine = routingEngine;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.createHttpServer()
                .requestHandler(routingEngine.createRouter())
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        LOGGER.info("Success");
                        startPromise.complete();
                    } else {
                        LOGGER.error("", result.cause());
                        result.cause().printStackTrace();
                        startPromise.fail(result.cause());
                    }
                });
    }
}
