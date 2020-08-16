package io.forestframework.core.http;

import com.google.inject.Injector;
import io.forestframework.core.http.routing.Routing;
import io.forestframework.core.http.routing.RoutingType;
import io.forestframework.core.http.sockjs.BridgeRequestHandler;
import io.forestframework.ext.core.HttpException;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.forestframework.core.http.RoutingMatchResults.HandlerMatchResult;

@Singleton
@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultHttpRequestHandler extends AbstractWebRequestHandler implements HttpRequestHandler {
    private static final CompletableFuture<Boolean> TRUE_FUTURE = CompletableFuture.completedFuture(Boolean.TRUE);
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpRequestHandler.class);

    private final RoutingMatcher routingMatcher;
    private final BridgeRequestHandler bridgeRequestHandler;

    @Inject
    public DefaultHttpRequestHandler(Vertx vertx,
                                     Injector injector,
                                     RoutingMatcher routingMatcher,
                                     BridgeRequestHandler bridgeRequestHandler) {
        super(vertx, injector);
        this.routingMatcher = routingMatcher;
        this.bridgeRequestHandler = bridgeRequestHandler;
    }

    @Override
    public void handle(HttpServerRequest request) {
        RoutingMatchResults matchResults = routingMatcher.match(request);

        if (isBridge(matchResults)) {
            bridgeRequestHandler.handle(request);
        } else {
            doHandle(request, matchResults);
        }
    }

    private boolean isWebSocket(HttpServerRequest matchResults) {
        return false;
    }

    private boolean isBridge(RoutingMatchResults matchResults) {
        return !matchResults.getMatchedRoutings(RoutingType.BRIDGE).isEmpty();
    }

    private void doHandle(HttpServerRequest request, RoutingMatchResults matchResults) {
        DefaultHttpContext context = new DefaultHttpContext(injector, request, matchResults);

        CompletableFuture<Boolean> preHandlerFuture = invokePreHandlers(context);

        composeSafely(preHandlerFuture, (Boolean shouldContinue) -> {
            if (Boolean.FALSE.equals(shouldContinue)) {
                // pre-handler says no
                // no error, call post handlers
                return invokePostHandlers(context);
            } else {
                HandlerMatchResult matchResult = context.getRoutingMatchResults().getHandlerMatchResult();
                if (matchResult.getStatusCode() != HttpStatusCode.OK) {
                    return invokeMatchedErrorHandler(context, new HttpException(matchResult.getStatusCode(), "Resource not found: " + request.path()))
                            .whenComplete((__, throwableInErrorHandler) -> invokePostHandlers(context, throwableInErrorHandler))
                            .exceptionally(__ -> COMPLETABLE_FUTURE_NIL);
                } else {
                    return invokeHandlers(context)
                            .whenComplete((__, throwableInHandler) -> {
                                        if (throwableInHandler == null) {
                                            invokePostHandlers(context);
                                        } else {
                                            invokeMatchedErrorHandler(context, throwableInHandler)
                                                    .whenComplete((___, throwableInErrorHandler) -> invokePostHandlers(context, throwableInHandler, throwableInErrorHandler));
                                        }
                                    }
                            )
                            .exceptionally(__ -> COMPLETABLE_FUTURE_NIL);
                }
            }
        }).exceptionally(throwableInPreHandlers ->
                invokeMatchedErrorHandler(context, throwableInPreHandlers)
                        .whenComplete((___, throwableInErrorHandler) -> invokePostHandlers(context, throwableInPreHandlers, throwableInErrorHandler))
        );
    }

    private CompletableFuture<Boolean> invokePreHandlers(DefaultHttpContext context) {
        CompletableFuture<Boolean> preHandlerFuture = TRUE_FUTURE;
        for (Routing preHandler : context.getRoutingMatchResults().getMatchedRoutings(RoutingType.PRE_HANDLER)) {
            preHandlerFuture = composeSafely(preHandlerFuture, (Boolean shouldContinue) -> {
                if (Boolean.FALSE.equals(shouldContinue)) {
                    // pre-handler says no
                    return CompletableFuture.completedFuture(Boolean.FALSE);
                } else {
                    return invokeRouting(preHandler, context);
                }
            });
        }
        return preHandlerFuture;
    }

    private <T, U> CompletableFuture<U> composeSafely(CompletableFuture<T> completableFuture, Function<? super T, ? extends CompletionStage<U>> fn) {
        return completableFuture.thenCompose((T t) -> {
            try {
                return fn.apply(t);
            } catch (Throwable e) {
                bugWarning(e);
                throw new RuntimeException(e);
            }
        });
    }

    // If matched error handler found, invoke it:
    //   If the error handler exit normally, returns a CompletableFuture which completes normally
    //   If the error handler exit abnormally, returns CompletableFuture with that failure
    // If matched error handler not found, returns a CompletableFuture with the original failure
    private CompletableFuture<Object> invokeMatchedErrorHandler(DefaultHttpContext context, Throwable throwable) {
        throwable = unwrap(throwable);

        HttpStatusCode statusCode = (throwable instanceof HttpException) ? ((HttpException) throwable).getCode() : HttpStatusCode.INTERNAL_SERVER_ERROR;
        context.getArgumentInjector().with(throwable).withParameter(HttpStatusCode.class, statusCode);

        Routing matchedErrorHandler = context.getRoutingMatchResults().getMatchedErrorHandler(statusCode);
        if (matchedErrorHandler == null) {
            return failedFuture(throwable);
        } else {
            return invokeRouting(context.getRoutingMatchResults().getMatchedErrorHandler(statusCode), context);
        }
    }

    private Throwable unwrap(Throwable t) {
        if (t instanceof CompletionException) {
            return t.getCause();
        } else {
            return t;
        }
    }

    private void logError(Throwable t) {
        LOGGER.error("", t);
    }

    // A finalizer which does the cleanup work:
    // If there're throwables, log them and return corresponding error code
    // End the response.
    private Object invokeFinalizingHandler(HttpContext context, Throwable... throwables) {
        try {
            Stream.of(throwables).forEach(this::logError);
            if (throwables.length > 0) {
                HttpStatusCode statusCode = IntStream.range(0, throwables.length)
                        .mapToObj(i -> throwables[throwables.length - i - 1])
                        .filter(t -> t instanceof HttpException)
                        .map(t -> ((HttpException) t).getCode())
                        .findFirst()
                        .orElse(HttpStatusCode.INTERNAL_SERVER_ERROR);
                if (!context.response().ended()) {
                    context.response().setStatusCode(statusCode.getCode());
                    context.response().write(statusCode.name());
                    ((EndForbiddenHttpServerResponseWrapper) context.response()).realEnd();
                }
            } else {
                if (!context.response().ended()) {
                    ((EndForbiddenHttpServerResponseWrapper) context.response()).realEnd();
                }
            }
            return COMPLETABLE_FUTURE_NIL;
        } catch (Throwable e) {
            bugWarning(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Invoke the post handlers with the potential throwables thrown from previous handlers.
     * It's guaranteed to return a CompletableFuture which exit normally.
     */
    private CompletableFuture<Object> invokePostHandlers(DefaultHttpContext context, Throwable... throwableThrownInPreviousHandlers) {
        CompletableFuture<Object> current = NIL_FUTURE;

        for (Routing postHandler : context.getRoutingMatchResults().getMatchedRoutings(RoutingType.POST_HANDLER)) {
            current = current.thenCompose(__ -> invokeRouting(postHandler, context));
        }

        return current
                .thenApply(__ -> invokeFinalizingHandler(context, throwableThrownInPreviousHandlers))
                .exceptionally(throwableInPostHandlers -> {
                    Throwable[] copy = Arrays.copyOf(throwableThrownInPreviousHandlers, throwableThrownInPreviousHandlers.length + 1);
                    copy[copy.length - 1] = throwableInPostHandlers;
                    return invokeFinalizingHandler(context, copy);
                });
    }

    // TODO remove it before publishing
    private void bugWarning(Throwable e) {
        LOGGER.error("Bug!", e);
    }

    /**
     * Invoke the real handlers. If no matching handlers found (404/405/406/415), invoke the corresponding error handler.
     */
    private CompletableFuture<Object> invokeHandlers(DefaultHttpContext context) {
        CompletableFuture<Object> current = NIL_FUTURE;

        for (Routing handler : context.getRoutingMatchResults().getMatchedRoutings(RoutingType.HANDLER)) {
            current = current.thenCompose(__ -> invokeRouting(handler, context));
        }

        return current;
    }
}
