package io.forestframework.core.http;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import io.forestframework.core.http.websocket.WebSocketRoutingContext;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.RoutingContext;
import org.apiguardian.api.API;

import java.nio.file.FileSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@API(status = API.Status.INTERNAL, since = "0.1")
public class InjectableParameters {
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

    public InjectableParameters(Injector injector) {
        this.injector = injector;
    }

    public InjectableParameters with(RoutingContext routingContext) {
        return withParameter(RoutingContext.class, routingContext)
                .withParameter(HttpServerRequest.class, routingContext.request())
                .withParameter(HttpServerResponse.class, routingContext.response());
    }

    public InjectableParameters with(WebSocketRoutingContext routingContext) {
        return withParameter(RoutingContext.class, routingContext);
    }

    public InjectableParameters with(RoutingContext routingContext, Throwable e) {
        InjectableParameters ret = with(routingContext)
                .withParameter(Throwable.class, e);
        if (e instanceof Exception) {
            ret.withParameter(Exception.class, (Exception) e);
        }
        return ret;
    }

    public <T> InjectableParameters withParameter(Class<T> klass, T argument) {
        parameters.put(klass, argument);
        return this;
    }

    @SuppressWarnings("unchecked")
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
