package io.forestframework.core.http.websocket;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.forestframework.core.http.AbstractWebRequestHandler;
import io.forestframework.core.http.DefaultHttpRequest;
import io.forestframework.core.http.HttpRequestHandler;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

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
        RoutingMatchResult.WebSocketRoutingMatchResult routingMatchResult = ((DefaultHttpRequest) request).getRoutingMatchResult();
        request.toWebSocket()
               .onSuccess(ws -> handleWebSocket(request, ws, routingMatchResult))
               .onFailure(e -> LOGGER.error("", e));
    }

    private void handleWebSocket(
        HttpServerRequest request,
        ServerWebSocket webSocket,
        RoutingMatchResult.WebSocketRoutingMatchResult routingMatchResult
    ) {
        Map<WebSocketEventType, WebSocketRouting> typeToHandlers = routingMatchResult.getRoutings();
        DefaultWebSocketContext context = new DefaultWebSocketContext(injector, request, webSocket, routingMatchResult);

        // Make sure all messages are processed in sequence
        AtomicReference<CompletableFuture<Object>> sequence = new AtomicReference<>(
            // OnWSOpen
            invokeWebSocketHandler(typeToHandlers, OPEN, context, null, null)
        );

        webSocket.handler(buffer -> {
            // OnWSMessage
            addMessageHandlerToSequence(sequence, __ -> invokeWebSocketHandler(typeToHandlers, MESSAGE, context, buffer, null));
        }).exceptionHandler(throwable -> {
            // OnWSError
            addMessageHandlerToSequence(sequence, __ -> invokeWebSocketHandler(typeToHandlers, ERROR, context, null, throwable));
        }).endHandler(v -> {
            // OnWSClose
            addMessageHandlerToSequence(sequence, __ -> invokeWebSocketHandler(typeToHandlers, CLOSE, context, null, null));
        });
    }

    private void addMessageHandlerToSequence(AtomicReference<CompletableFuture<Object>> sequence, Function<Object, CompletableFuture<Object>> next) {
        sequence.set(
            sequence.get().thenCompose(next)
        );
    }

    private CompletableFuture<Object> invokeWebSocketHandler(Map<WebSocketEventType, WebSocketRouting> routings, WebSocketEventType eventType, DefaultWebSocketContext context, Buffer buffer, Throwable throwable) {
        WebSocketRouting routing = routings.get(eventType);
        if (routing != null) {
            context.getArgumentInjector()
                   .withParameter(WebSocketEventType.class, eventType)
                   .withParameter(Buffer.class, buffer)
                   .withParameterSupplier(String.class, buffer == null ? null : buffer::toString)
                   .with(throwable);

            return invokeRoutingWithoutProcessingResult(routing, context)
                .whenComplete((returnValue, failure) -> {
                    if (failure != null) {
                        LOGGER.error("", failure);
                    }
                });
        } else {
            return NIL_FUTURE;
        }
    }
}
