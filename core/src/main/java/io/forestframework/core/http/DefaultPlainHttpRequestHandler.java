package io.forestframework.core.http;

import com.google.inject.Injector;
import io.forestframework.core.http.routing.PlainHttpRoutingMatchResult;
import io.forestframework.core.http.routing.Routing;
import io.forestframework.core.http.routing.RoutingType;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;
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
        PlainHttpRoutingMatchResult routingMatchResult = ((DefaultHttpRequest) request).getRoutingMatchResult();

        DefaultHttpContext context = new DefaultHttpContext(injector, (DefaultHttpRequest) request, routingMatchResult);

        CompletableFuture<Boolean> preHandlerFuture = invokePreHandlers(context, routingMatchResult);

        composeSafely(preHandlerFuture, (Boolean shouldContinue) -> {
            if (Boolean.FALSE.equals(shouldContinue)) {
                // pre-handler says no
                // no error, call post handlers
                return invokePostHandlers(context, routingMatchResult);
            } else {
                MainHandlerMatchResult mainHandlerMatchResult = routingMatchResult.getMainHandlerMatchResult();
                HttpStatusCode statusCode = mainHandlerMatchResult.getStatusCode();
                if (mainHandlerMatchResult.getStatusCode() != HttpStatusCode.OK) {
                    return handleError(context,
                                       routingMatchResult,
                                       statusCode,
                                       new HttpException(mainHandlerMatchResult.getStatusCode(), mainHandlerMatchResult.getStatusCode().name() + ", request path: " + request.path()));
                } else {
                    return invokeMainHandler(context, routingMatchResult)
                        .whenComplete((__, throwableInHandler) -> {
                            if (throwableInHandler == null) {
                                invokePostHandlers(context, routingMatchResult);
                            } else {
                                handleError(context, routingMatchResult, getStatusCode(throwableInHandler), throwableInHandler);
                            }
                        })
                        .exceptionally(__ -> COMPLETABLE_FUTURE_NIL);
                }
            }
        }).exceptionally(throwableInPreHandlers -> handleError(context, routingMatchResult, getStatusCode(throwableInPreHandlers), throwableInPreHandlers));
    }

    private CompletableFuture<Object> handleError(DefaultHttpContext context, PlainHttpRoutingMatchResult routingMatchResult, HttpStatusCode statusCode, Throwable throwable) {
        Routing matchedErrorHandler = routingMatchResult.getMatchingErrorHandler(statusCode);
        if (matchedErrorHandler == null) {
            // No matched error handler, invoke post handler directly
            return invokePostHandlers(context, routingMatchResult, throwable);
        } else {
            return invokeMatchedErrorHandler(context, matchedErrorHandler, statusCode, throwable)
                .whenComplete((___, throwableInErrorHandler) -> invokePostHandlers(context, routingMatchResult, throwableInErrorHandler));
        }
    }

    private CompletableFuture<Boolean> invokePreHandlers(DefaultHttpContext context, PlainHttpRoutingMatchResult routingMatchResult) {
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

    private HttpStatusCode getStatusCode(Throwable t) {
        Throwable throwable = unwrap(t);

        return (throwable instanceof HttpException) ? ((HttpException) throwable).getCode() : HttpStatusCode.INTERNAL_SERVER_ERROR;
    }

    // If matched error handler found, invoke it:
    //   If the error handler exit normally, returns a CompletableFuture which completes normally
    //   If the error handler exit abnormally, returns CompletableFuture with that failure
    // If matched error handler not found, returns a CompletableFuture with the original failure
//    private CompletableFuture<Object> invokeMatchedErrorHandler(DefaultHttpContext context, PlainHttpRoutingMatchResult routingMatchResult, Throwable t) {
//        Throwable throwable = unwrap(t);
//
//        HttpStatusCode statusCode = (throwable instanceof HttpException) ? ((HttpException) throwable).getCode() : HttpStatusCode.INTERNAL_SERVER_ERROR;
//        if (context.response().headWritten() && context.response().getStatusCode() != statusCode.getCode()) {
//            LOGGER.warn("Trying to set status code " + statusCode.getCode() + " but header is written.");
//        }
//        context.getArgumentInjector().with(throwable).withParameter(HttpStatusCode.class, statusCode);
//
//        Routing matchedErrorHandler = routingMatchResult.getMatchingErrorHandler(statusCode);
//        if (matchedErrorHandler == null) {
//            return failedFuture(throwable);
//        } else {
//            return invokeRouting(matchedErrorHandler, context);
//        }
//    }
    private CompletableFuture<Object> invokeMatchedErrorHandler(DefaultHttpContext context, Routing matchedErrorHandler, HttpStatusCode statusCode, Throwable t) {
        safeSetStatusCode(context.response(), statusCode);
        context.getArgumentInjector().with(t).withParameter(HttpStatusCode.class, statusCode);
        return invokeRouting(matchedErrorHandler, context);
    }

    private Throwable unwrap(Throwable t) {
        if (t instanceof CompletionException) {
            return t.getCause();
        } else {
            return t;
        }
    }

    private void logError(HttpContext context, Throwable t) {
        if ("/favicon.ico".equals(context.request().path()) && t instanceof HttpException && ((HttpException) t).getCode() == HttpStatusCode.NOT_FOUND) {
            // TODO test this special case
            LOGGER.debug("", t);
        } else {
            LOGGER.error("", t);
        }
    }

    // A finalizer which does the cleanup work:
    // If there're throwables, log them and return corresponding error code
    // End the response.
    private Object invokeFinalizingHandler(HttpContext context, Throwable... uncaughtThrowables) {
        try {
            List<Throwable> realThrowables = Stream.of(uncaughtThrowables)
                                                   .filter(Objects::nonNull)
                                                   .peek(e -> logError(context, e))
                                                   .collect(Collectors.toList());
            if (realThrowables.isEmpty()) {
                if (!context.response().ended()) {
                    context.response().end();
                }
            } else {
                HttpStatusCode statusCode = realThrowables
                    .stream()
                    .filter(t -> t instanceof HttpException)
                    .map(t -> ((HttpException) t).getCode())
                    .findFirst()
                    .orElse(HttpStatusCode.INTERNAL_SERVER_ERROR);
                HttpResponse response = context.response();
                if (!response.ended()) {
                    safeSetStatusCode(response, statusCode);
                    response.end(statusCode.name());
                }
            }
            return COMPLETABLE_FUTURE_NIL;
        } catch (Throwable e) {
            bugWarning(e);
            throw new RuntimeException(e);
        }
    }

    private void safeSetStatusCode(HttpResponse response, HttpStatusCode statusCode) {
        if (response.headWritten() && response.getStatusCode() != statusCode.getCode()) {
            LOGGER.warn("Trying to set status code " + statusCode.getCode() + " but header is writen.");
        } else {
            response.setStatusCode(statusCode.getCode());
        }
    }

    /**
     * Invoke the post handlers with the potential throwables thrown from previous handlers but uncaught.
     * It's guaranteed to return a CompletableFuture which exit normally.
     */
    private CompletableFuture<Object> invokePostHandlers(DefaultHttpContext context, PlainHttpRoutingMatchResult routingMatchResult, Throwable... uncaughtThrowables) {
        CompletableFuture<Object> current = NIL_FUTURE;

        for (Routing postHandler : routingMatchResult.getMatchingHandlersByType(RoutingType.POST_HANDLER)) {
            current = current.thenCompose(__ -> invokeRouting(postHandler, context));
        }

        return current
            .thenApply(__ -> invokeFinalizingHandler(context, uncaughtThrowables))
            .exceptionally(throwableInPostHandlers -> {
                Throwable[] copy = Arrays.copyOf(uncaughtThrowables, uncaughtThrowables.length + 1);
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
    private CompletableFuture<Object> invokeMainHandler(DefaultHttpContext context, PlainHttpRoutingMatchResult routingMatchResult) {
        List<Routing> mainHandlers = routingMatchResult.getMatchingHandlersByType(RoutingType.HANDLER);
        if (mainHandlers.size() > 1) {
            LOGGER.warn("Found more than 1 main handlers when handling " + context.request().path() + ", only the first one will be invoked.");
        }

        return invokeRouting(mainHandlers.get(0), context);
    }
}
