package io.forestframework.core.http.websocket;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.forestframework.core.http.AbstractWebRequestHandler;
import io.forestframework.core.http.HttpRequestHandler;
import io.forestframework.core.http.HttpServerRequestWrapper;
import io.forestframework.core.http.routing.RoutingMatchResult;
import io.forestframework.core.http.routing.WebSocketRouting;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

import static io.forestframework.core.http.websocket.WebSocketEventType.CLOSE;
import static io.forestframework.core.http.websocket.WebSocketEventType.ERROR;
import static io.forestframework.core.http.websocket.WebSocketEventType.MESSAGE;
import static io.forestframework.core.http.websocket.WebSocketEventType.OPEN;

@Singleton
@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultWebSocketRequestHandler extends AbstractWebRequestHandler implements HttpRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWebSocketRequestHandler.class);

    @Inject
    public DefaultWebSocketRequestHandler(Vertx vertx,
                                          Injector injector) {
        super(vertx, injector);
    }

    @Override
    public void handle(HttpServerRequest request) {
        RoutingMatchResult.WebSocketRoutingMatchResult routingMatchResult = ((HttpServerRequestWrapper) request).getRoutingMatchResult();
        Map<WebSocketEventType, WebSocketRouting> typeToHandlers = routingMatchResult.getRoutings();

        ServerWebSocket webSocket = request.upgrade();

        DefaultWebSocketContext context = new DefaultWebSocketContext(injector, webSocket, routingMatchResult);

        // OnWSOpen
        invokeWebSocketHandler(typeToHandlers, OPEN, context, null, null);

        webSocket.handler(buffer -> {
            // OnWSMessage
            invokeWebSocketHandler(typeToHandlers, MESSAGE, context, buffer, null);
        }).exceptionHandler(throwable -> {
            // OnWSError
            invokeWebSocketHandler(typeToHandlers, ERROR, context, null, throwable);
        }).endHandler(v -> {
            // OnWSClose
            invokeWebSocketHandler(typeToHandlers, CLOSE, context, null, null);
        });
    }

    private void invokeWebSocketHandler(Map<WebSocketEventType, WebSocketRouting> routings, WebSocketEventType eventType, DefaultWebSocketContext context, Buffer buffer, Throwable throwable) {
        WebSocketRouting routing = routings.get(eventType);
        if (routing != null) {
            context.getArgumentInjector()
                    .withParameter(WebSocketEventType.class, eventType)
                    .withParameter(Buffer.class, buffer)
                    .withParameterSupplier(String.class, buffer == null ? null : buffer::toString)
                    .with(throwable);

            invokeRouting(routing, context)
                    .whenComplete((returnValue, failure) -> {
                        if (failure != null) {
                            LOGGER.error("", failure);
                        }
                    });
        }
    }
}
