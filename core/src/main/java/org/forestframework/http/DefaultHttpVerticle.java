package org.forestframework.http;

import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

public class DefaultHttpVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpVerticle.class);
    private final Vertx vertx;
    private final RoutingEngine routingEngine;
    private final HttpServerOptions httpServerOptions;

    @Inject
    public DefaultHttpVerticle(Vertx vertx,
                               RoutingEngine routingEngine,
                               @Named("forest.http") HttpServerOptions httpServerOptions) {
        this.vertx = vertx;
        this.routingEngine = routingEngine;
        this.httpServerOptions = httpServerOptions;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        try {
            vertx.createHttpServer(httpServerOptions)
                    .requestHandler(routingEngine.createRouter())
                    .listen(result -> {
                        if (result.succeeded()) {
                            LOGGER.info("Success");
                            startPromise.complete();
                        } else {
                            LOGGER.error("", result.cause());
                            result.cause().printStackTrace();
                            startPromise.fail(result.cause());
                        }
                    });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
