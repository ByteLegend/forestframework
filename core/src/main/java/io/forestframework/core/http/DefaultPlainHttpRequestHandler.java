package io.forestframework.core.http;

import com.google.inject.Injector;
import io.forestframework.core.http.routing.PlainHttpRoutingMatchResult;
import io.forestframework.core.http.routing.Routing;
import io.forestframework.core.http.routing.RoutingType;
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

import static io.forestframework.core.http.routing.PlainHttpRoutingMatchResult.MainHandlerMatchResult;


@Singleton
@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultPlainHttpRequestHandler extends AbstractWebRequestHandler implements HttpRequestHandler {
    private static final CompletableFuture<Boolean> TRUE_FUTURE = CompletableFuture.completedFuture(Boolean.TRUE);
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPlainHttpRequestHandler.class);


    @Inject
    public DefaultPlainHttpRequestHandler(Vertx vertx, Injector injector) {
        super(vertx, injector);
    }

    @Override
    public void handle(HttpServerRequest request) {
        PlainHttpRoutingMatchResult routingMatchResult = ((HttpServerRequestWrapper) request).getRoutingMatchResult();

        DefaultPlainHttpContext context = new DefaultPlainHttpContext(injector, request, routingMatchResult);

        CompletableFuture<Boolean> preHandlerFuture = invokePreHandlers(context, routingMatchResult);

        composeSafely(preHandlerFuture, (Boolean shouldContinue) -> {
            if (Boolean.FALSE.equals(shouldContinue)) {
                // pre-handler says no
                // no error, call post handlers
                return invokePostHandlers(context, routingMatchResult);
            } else {
                MainHandlerMatchResult mainHandlerMatchResult = routingMatchResult.getMainHandlerMatchResult();
                if (mainHandlerMatchResult.getStatusCode() != HttpStatusCode.OK) {
                    return invokeMatchedErrorHandler(context, routingMatchResult, new HttpException(mainHandlerMatchResult.getStatusCode(), "Resource not found: " + request.path()))
                            .whenComplete((__, throwableInErrorHandler) -> invokePostHandlers(context, routingMatchResult, throwableInErrorHandler))
                            .exceptionally(__ -> COMPLETABLE_FUTURE_NIL);
                } else {
                    return invokeHandlers(context, routingMatchResult)
                            .whenComplete((__, throwableInHandler) -> {
                                        if (throwableInHandler == null) {
                                            invokePostHandlers(context, routingMatchResult);
                                        } else {
                                            invokeMatchedErrorHandler(context, routingMatchResult, throwableInHandler)
                                                    .whenComplete((___, throwableInErrorHandler) -> invokePostHandlers(context, routingMatchResult, throwableInHandler, throwableInErrorHandler));
                                        }
                                    }
                            )
                            .exceptionally(__ -> COMPLETABLE_FUTURE_NIL);
                }
            }
        }).exceptionally(throwableInPreHandlers ->
                invokeMatchedErrorHandler(context, routingMatchResult, throwableInPreHandlers)
                        .whenComplete((___, throwableInErrorHandler) -> invokePostHandlers(context, routingMatchResult, throwableInPreHandlers, throwableInErrorHandler))
        );
    }

    private CompletableFuture<Boolean> invokePreHandlers(DefaultPlainHttpContext context, PlainHttpRoutingMatchResult routingMatchResult) {
        CompletableFuture<Boolean> preHandlerFuture = TRUE_FUTURE;
        for (Routing preHandler : routingMatchResult.getMatchingHandlersByType(RoutingType.PRE_HANDLER)) {
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
    private CompletableFuture<Object> invokeMatchedErrorHandler(DefaultPlainHttpContext context, PlainHttpRoutingMatchResult routingMatchResult, Throwable t) {
        Throwable throwable = unwrap(t);

        HttpStatusCode statusCode = (throwable instanceof HttpException) ? ((HttpException) throwable).getCode() : HttpStatusCode.INTERNAL_SERVER_ERROR;
        context.getArgumentInjector().with(throwable).withParameter(HttpStatusCode.class, statusCode);

        Routing matchedErrorHandler = routingMatchResult.getMatchingErrorHandler(statusCode);
        if (matchedErrorHandler == null) {
            return failedFuture(throwable);
        } else {
            return invokeRouting(matchedErrorHandler, context);
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
    private Object invokeFinalizingHandler(PlainHttpContext context, Throwable... throwables) {
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
                    context.response().setStatusCode(statusCode.getCode()).write(statusCode.name());
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
    private CompletableFuture<Object> invokePostHandlers(DefaultPlainHttpContext context, PlainHttpRoutingMatchResult routingMatchResult, Throwable... throwableThrownInPreviousHandlers) {
        CompletableFuture<Object> current = NIL_FUTURE;

        for (Routing postHandler : routingMatchResult.getMatchingHandlersByType(RoutingType.POST_HANDLER)) {
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
    private CompletableFuture<Object> invokeHandlers(DefaultPlainHttpContext context, PlainHttpRoutingMatchResult routingMatchResult) {
        CompletableFuture<Object> current = NIL_FUTURE;

        for (Routing handler : routingMatchResult.getMatchingHandlersByType(RoutingType.HANDLER)) {
            current = current.thenCompose(__ -> invokeRouting(handler, context));
        }

        return current;
    }
}
