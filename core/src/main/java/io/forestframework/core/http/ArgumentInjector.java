package io.forestframework.core.http;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import io.forestframework.core.http.websocket.WebSocketContext;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.shareddata.SharedData;
import org.apiguardian.api.API;

import java.nio.file.FileSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * For internal usage only.
 */
@API(status = API.Status.INTERNAL, since = "0.1")
public class ArgumentInjector {
    /**
     * The global singletons for parameter injection, see {@link io.forestframework.core.CoreModule}
     */
    @SuppressWarnings("JavadocReference")
    private static final Set<Class<?>> SUPPORTED_CLASSES = ImmutableSet.of(
            Vertx.class,
            EventBus.class,
            SharedData.class,
            FileSystem.class
    );
    // Must be HashMap because it allows null
    // It's thread-safe: only created and used in same thread
    private final Injector injector;
    private final Map<Class<?>, Object> parameters = new HashMap<>();

    public ArgumentInjector(Injector injector) {
        this.injector = injector;
    }

    public ArgumentInjector with(PlainHttpContext context) {
        return withParameter(WebContext.class, context)
                .withParameter(PlainHttpContext.class, context)
                .withParameter(HttpServerRequest.class, context.request())
                .withParameter(HttpServerResponse.class, context.response());
    }

    public ArgumentInjector with(WebSocketContext context) {
        return withParameter(WebSocketContext.class, context)
                .withParameter(WebContext.class, context)
                .withParameter(ServerWebSocket.class, context.webSocket());
    }

    public ArgumentInjector with(Throwable t) {
        withParameter(Throwable.class, t);
        if (t instanceof RuntimeException || t == null) {
            withParameter(RuntimeException.class, (RuntimeException) t);
        }
        if (t instanceof Exception || t == null) {
            withParameter(Exception.class, (Exception) t);
        }
        return this;
    }
//
//    public InjectableParameters with(WebSocketRoutingContext routingContext) {
//        return withParameter(RoutingContext.class, routingContext);
//    }
//
//    public InjectableParameters with(RoutingContext routingContext, Throwable e) {
//        InjectableParameters ret = with(routingContext)
//                .withParameter(Throwable.class, e);
//        if (e instanceof Exception) {
//            ret.withParameter(Exception.class, (Exception) e);
//        }
//        return ret;
//    }

    public <T> ArgumentInjector withParameter(Class<T> klass, T argument) {
        parameters.put(klass, argument);
        return this;
    }

    public <T> ArgumentInjector withParameterSupplier(Class<T> klass, Supplier<T> supplier) {
        parameters.put(klass, supplier);
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T resolve(Class<T> klass, Supplier<String> errorMessage) {
        if (SUPPORTED_CLASSES.contains(klass)) {
            return injector.getInstance(klass);
        }

        if (parameters.containsKey(klass)) {
            Object ret = parameters.get(klass);
            if (ret instanceof Supplier) {
                return (T) ((Supplier) ret).get();
            } else {
                return (T) ret;
            }
        }

        for (Map.Entry<Class<?>, Object> entry : parameters.entrySet()) {
            if (entry.getKey().isAssignableFrom(klass)) {
                return (T) entry.getValue();
            }
        }
        throw new IllegalArgumentException(errorMessage.get());
    }
}
