package io.forestframework.core.http.routing;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import com.google.inject.Injector;
import io.forestframework.core.config.Config;
import io.forestframework.core.http.FastRoutingCompatible;
import io.forestframework.core.http.HttpMethod;
import io.forestframework.core.http.HttpStatusCode;
import io.forestframework.core.http.OptimizedHeaders;
import io.forestframework.core.http.param.ParameterResolver;
import io.forestframework.core.http.result.ResultProcessor;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

/**
 * A "fast" routing engine implementation which only handles "fast" routing. A routing is fast when:
 *
 * <ol>
 *     <li>1. No regex/path parameters. Path matching is slow.</li>
 *     <li>2. No complicated {@link RoutingContext} operations in param resolvers and result processors. This is guaranteed by {@link FastRoutingCompatible}.
 *     <li>3. No prefix-matching interceptors. The prefix scanning is performed at startup phase.</li>
 * </ol>
 *
 * Fast routing is matched by hashtable lookup and invoked directly, without {@link io.vertx.ext.web.Router}/{@link RoutingContext} creation.
 */
@Singleton
public class FastRequestHandler extends AbstractRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FastRequestHandler.class);
    private final DefaultRoutings routings;
    private final Map<String, List<Routing>> pathToFastRoutingsMap;

    @Inject
    public FastRequestHandler(Injector injector,
                              Vertx vertx,
                              Routings routings,
                              @Config("forest.environment") String environment) {
        super(vertx, injector, routings, environment);
        this.routings = (DefaultRoutings) routings;
        this.pathToFastRoutingsMap = createPathToFastRoutingsMap();
    }

    @Override
    public void handle(HttpServerRequest request) {
        Routing routing = findFastRouting(request);

        if (routing != null) {
            doHandle(routing, request);
        } else {
            next(request);
        }
    }

    private Routing findFastRouting(HttpServerRequest request) {
        HttpMethod method = HttpMethod.fromVertHttpMethod(request.method());
        return pathToFastRoutingsMap.getOrDefault(request.path(), Collections.emptyList()).stream()
                .filter(routing -> routing.getMethods().contains(method))
                .findFirst()
                .orElse(null);
    }

    private void doHandle(Routing routing, HttpServerRequest request) {
        RoutingContext context = new FastRoutingContext(vertx, request);
        Object[] arguments = resolveArguments(routing, context);
        CompletableFuture<Object> returnValueFuture = invokeHandler(routing, arguments);
        returnValueFuture.whenComplete((returnValue, failure) -> {
            if (failure == null) {
                processResult(context, routing, returnValue);
            } else {
                onHandlerFailure(context, failure);
            }
        });
    }

    private void processResult(RoutingContext context, Routing routing, Object handlerReturnValue) {
        invokeResultProcessors(context, routing, handlerReturnValue).whenComplete((resultProcessorReturnValue, failure) -> {
            if (failure != null) {
                onHandlerFailure(context, failure);
            }
            if (!context.response().ended()) {
                context.response().end();
            }
        });
    }

    private Map<String, List<Routing>> createPathToFastRoutingsMap() {
        return routings.getRouting(RoutingType.HANDLER)
                .stream()
                .filter(this::isFastRouting)
                .collect(groupingBy(Routing::getPath));
    }

    private boolean isFastRouting(Routing routing) {
        return routing.getRegexPath().isEmpty() &&
                !containsPathParameter(routing) &&
                noPrefixMatchingInterceptors(routing) &&
                allParamResolversAndResultProcessorsAreFast(routing);
    }

    private boolean containsPathParameter(Routing routing) {
        for (char ch : routing.getPath().toCharArray()) {
            if (ch == ':' || ch == '*') {
                return true;
            }
        }
        return false;
    }

    private boolean allParamResolversAndResultProcessorsAreFast(Routing routing) {
        for (int i = 0; i < routing.getHandlerMethod().getParameters().length; ++i) {
            ParameterResolver resolver = AnnotationMagic.getOneAnnotationOnMethodParameterOrNull(routing.getHandlerMethod(), i, ParameterResolver.class);
            if (resolver != null && !resolver.by().isAnnotationPresent(FastRoutingCompatible.class)) {
                return false;
            }
        }

        return AnnotationMagic.getAnnotationsOnMethod(routing.getHandlerMethod(), ResultProcessor.class)
                .stream()
                .map(ResultProcessor::by)
                .allMatch(klass -> klass.isAnnotationPresent(FastRoutingCompatible.class));
    }

    private boolean noPrefixMatchingInterceptors(Routing routing) {
        return Stream.of(RoutingType.values())
                .filter(routingType -> routingType != RoutingType.HANDLER)
                .map(routings::getRoutingPrefixes)
                .flatMap(List::stream)
                .noneMatch(prefix -> routing.getPath().startsWith(prefix));
    }

    void onHandlerFailure(RoutingContext context, Throwable failure) {
        LOGGER.error("", failure);
        context.response().setStatusCode(HttpStatusCode.SERVER_ERROR.getCode());
        context.response().putHeader(OptimizedHeaders.HEADER_CONTENT_TYPE, OptimizedHeaders.CONTENT_TYPE_TEXT_PLAIN);
        context.response().end(ExceptionUtils.getStackTrace(failure));
    }
}
