package io.forestframework.core.http.sockjs;

import com.google.inject.Injector;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.http.AbstractWebRequestHandler;
import io.forestframework.core.http.HttpRequestHandler;
import io.forestframework.core.http.routing.BridgeRouting;
import io.forestframework.core.http.routing.RoutingManager;
import io.forestframework.core.http.routing.RoutingType;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * For internal use only.
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
@Singleton
public class BridgeRequestHandler extends AbstractWebRequestHandler implements HttpRequestHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(BridgeRequestHandler.class);
    private final Router router;
    private final ConfigProvider configProvider;

    @Inject
    public BridgeRequestHandler(Vertx vertx, Injector injector, RoutingManager routingManager, ConfigProvider configProvider) {
        super(vertx, injector);
        this.configProvider = configProvider;
        router = createRouter(vertx, routingManager);
    }

    private void configure(Router router, String path, Map<BridgeEventType, BridgeRouting> routings) {
        SockJSBridgeOptions options = configProvider.getInstance("forest.bridge", SockJSBridgeOptions.class);
//        SockJSBridgeOptions options = new SockJSBridgeOptions()
//                .addOutboundPermitted(new PermittedOptions().setAddressRegex("auction\\.[0-9]+"));

        path = removeTailSlashAndStars(path);

        router.mountSubRouter(path, SockJSHandler.create(vertx).bridge(options, event -> {
            BridgeContext context = new BridgeContext(injector);

            if (event.type() == io.vertx.ext.bridge.BridgeEventType.SOCKET_CREATED) {
                event.socket().exceptionHandler(e -> {
                    // onError
                    invokeOnError(e, context, routings.get(BridgeEventType.SOCKET_ERROR));
                });
            }

            BridgeRouting routing = routings.get(BridgeEventType.fromVertxType(event.type()));
            if (routing == null) {
                event.complete(true);
            } else {
                invokeHandler(routing, context, new BridgeEventAdapter(event));
            }
        }));
    }

    private void invokeHandler(BridgeRouting routing, BridgeContext context, BridgeEvent event) {
        context.getArgumentInjector()
                .withParameter(BridgeEvent.class, event)
                .withParameter(BridgeEventType.class, event.type());

        invokeRouting(routing, context).whenComplete((result, throwable) -> {
            if (throwable != null) {
                LOGGER.error("", throwable);
            } else if (Boolean.FALSE.equals(result)) {
                event.tryComplete(false);
            } else {
                event.tryComplete(true);
            }
        });
    }

    private String removeTailSlashAndStars(String path) {
        for (int i = path.length() - 1; i >= 0; i--) {
            if (path.charAt(i) != '/' && path.charAt(i) != '*') {
                return path.substring(0, i + 1);
            }
        }
        throw new IllegalArgumentException("Invalid path: " + path);
    }

    private void invokeOnError(Throwable e, BridgeContext context, BridgeRouting onErrorHandler) {
        if (onErrorHandler == null) {
            LOGGER.error("", e);
            return;
        }
        context.getArgumentInjector()
                .with(e)
                .withParameter(BridgeEventType.class, BridgeEventType.SOCKET_ERROR);
        invokeRouting(onErrorHandler, context).whenComplete((__, throwable) -> {
            if (throwable != null) {
                LOGGER.error("", e);
            }
        });
    }

    private Router createRouter(Vertx vertx, RoutingManager routingManager) {
        Router router = Router.router(vertx);

        // A single path must has only one handler per event type
        Map<String, List<BridgeRouting>> pathToHandlers = routingManager.getRouting(RoutingType.BRIDGE)
                .stream()
                .map(r -> (BridgeRouting) r)
                .collect(Collectors.groupingBy(BridgeRouting::getPath));

        pathToHandlers.forEach((key, value) -> configure(router, key, validateAndRemapBridgeRoutings(key, value)));
        return router;
    }

    @Override
    public void handle(HttpServerRequest request) {
        router.handle(request);
    }

    private Map<BridgeEventType, BridgeRouting> validateAndRemapBridgeRoutings(String path, List<BridgeRouting> routings) {
        Map<BridgeEventType, BridgeRouting> ret = new HashMap<>();
        for (BridgeRouting routing : routings) {
            for (BridgeEventType type : routing.getEventTypes()) {
                BridgeRouting oldRouting = ret.put(type, routing);
                if (oldRouting != null && !routing.getHandlerMethod().equals(oldRouting.getHandlerMethod())) {
                    // For a specific path, there should not be more than one handler mapped to the same event.
                    throw new IllegalArgumentException("Found more than one Bridge handler mapped to " + path + " on event " + type
                            + ":\n1. " + oldRouting.getHandlerMethod() + "\n2. " + routing.getHandlerMethod());
                }
            }
        }
        return ret;
    }

}
