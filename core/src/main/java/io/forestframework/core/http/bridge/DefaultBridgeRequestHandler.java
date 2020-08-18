package io.forestframework.core.http.bridge;

import com.google.inject.Injector;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.http.AbstractWebRequestHandler;
import io.forestframework.core.http.HttpRequestHandler;
import io.forestframework.core.http.routing.BridgeRouting;
import io.forestframework.core.http.routing.DefaultRoutingManager;
import io.forestframework.core.http.routing.RoutingManager;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * For internal use only.
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
@Singleton
public class DefaultBridgeRequestHandler extends AbstractWebRequestHandler implements HttpRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBridgeRequestHandler.class);
    private final Router router;
    private final ConfigProvider configProvider;

    @Inject
    public DefaultBridgeRequestHandler(Vertx vertx, Injector injector, RoutingManager routingManager, ConfigProvider configProvider) {
        super(vertx, injector);
        this.configProvider = configProvider;
        this.router = createRouter(vertx, routingManager);
    }

    @Override
    public void handle(HttpServerRequest request) {
        router.handle(request);
    }

    private void configure(Router router, String path, Map<BridgeEventType, BridgeRouting> routings) {
        router.mountSubRouter(path, SockJSHandler.create(vertx).bridge(getOptions(path), event -> {
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

    @SuppressWarnings("unchecked")
    private SockJSBridgeOptions getOptions(String path) {
        Map<String, Map<String, Object>> allOptions = configProvider.getInstance("forest.bridge", Map.class);
        Map<String, Object> optionsOfPath = allOptions.get(path);
        if (optionsOfPath == null) {
            return new SockJSBridgeOptions();
        } else {
            JsonObject jsonObject = new JsonObject(optionsOfPath);
            SockJSBridgeOptions options = new SockJSBridgeOptions(jsonObject);
            if (jsonObject.getValue("inboundPermitteds") instanceof JsonArray) {
                ((Iterable<Object>) jsonObject.getValue("inboundPermitteds")).forEach(item -> options.addInboundPermitted(new PermittedOptions((JsonObject) item)));
            }
            if (jsonObject.getValue("outboundPermitteds") instanceof JsonArray) {
                ((Iterable<Object>) jsonObject.getValue("outboundPermitteds")).forEach(item -> options.addOutboundPermitted(new PermittedOptions((JsonObject) item)));
            }
            return options;
        }
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

        ((DefaultRoutingManager) routingManager).getBridgeRoutings()
                .forEach((key, value) -> configure(router, key, value));
        return router;
    }
}
