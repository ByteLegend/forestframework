package io.forestframework.core.http.routing;


import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.forestframework.core.ComponentClasses;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.core.AutoRoutingScanExtension;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

class PreHandlerReturnsString {
    @Route(value = "/**", type = RoutingType.PRE_HANDLER)
    public String preHandler(HttpServerRequest request) {
        return "shouldNotBeHere";
    }

    @Route(value = "/**", type = RoutingType.HANDLER)
    public void handler(HttpServerRequest request, HttpServerResponse response) {
        throw new UnsupportedOperationException();
    }
}

class PreHandlerReturnsFutureString extends AbstractTraceableRouter {
    @Route(value = "/**", type = RoutingType.PRE_HANDLER)
    public CompletableFuture<String> preHandler() {
        return new CompletableFuture<>();
    }

    @Route(value = "/**", type = RoutingType.HANDLER)
    public void handler() {
        throw new UnsupportedOperationException();
    }
}

class PreHandlerReturnsVoid {
    @Route(value = "/**", type = RoutingType.PRE_HANDLER)
    public void preHandler(HttpServerRequest request) {
        throw new UnsupportedOperationException();
    }

    @Route(value = "/**", type = RoutingType.HANDLER)
    public void handler(HttpServerRequest request, HttpServerResponse response) {
        throw new UnsupportedOperationException();
    }
}

class PreHandlerReturnsBoolean {
    @Route(value = "/**", type = RoutingType.PRE_HANDLER)
    public Boolean preHandler(HttpServerRequest request) {
        return Boolean.TRUE;
    }

    @Route(value = "/**", type = RoutingType.HANDLER)
    public void handler(HttpServerRequest request, HttpServerResponse response) {
        throw new UnsupportedOperationException();
    }
}

class PreHandlerReturnsbooleanType {
    @Route(value = "/**", type = RoutingType.PRE_HANDLER)
    public boolean preHandler(HttpServerRequest request) {
        return false;
    }

    @Route(value = "/**", type = RoutingType.HANDLER)
    public void handler() {
        throw new UnsupportedOperationException();
    }
}

class PreHandlerReturnsCompletableFutureVoid {
    @Route(value = "/**", type = RoutingType.PRE_HANDLER)
    public CompletableFuture<Void> preHandler(HttpServerRequest request) {
        return new CompletableFuture<>();
    }

    @Route(value = "/**", type = RoutingType.HANDLER)
    public void handler(HttpServerRequest request, HttpServerResponse response) {
        throw new UnsupportedOperationException();
    }
}

class PreHandlerReturnsCompletableFutureBoolean {
    @Route(value = "/**", type = RoutingType.PRE_HANDLER)
    public CompletableFuture<Boolean> preHandler(HttpServerRequest request) {
        return new CompletableFuture<>();
    }

    @Route(value = "/**", type = RoutingType.HANDLER)
    public void handler(HttpServerRequest request, HttpServerResponse response) {
        throw new UnsupportedOperationException();
    }
}

@ExtendWith(MockitoExtension.class)
public class StartupTimePreHandlerReturnTypeTest {
    Extension autoRoutingScanExtension = new AutoRoutingScanExtension();

    @Mock
    Injector injector;

    @Mock
    RoutingManager mockManager;

    List<Class<?>> mockComponentClasses = new ArrayList<>();

    @BeforeEach
    void init() {
        // @formatter:off
        when(injector.getInstance(Key.get(new TypeLiteral<List<Class<?>>>() { }, ComponentClasses.class))).thenReturn(mockComponentClasses);
        // @formatter:on

        when(injector.getInstance(RoutingManager.class)).thenReturn(mockManager);
    }

    @ParameterizedTest
    @ValueSource(classes = {PreHandlerReturnsString.class, PreHandlerReturnsFutureString.class})
    void throwRuntimeExceptionWhenPreHandlerReturnTypeIsNotValid(Class<?> cls) {
        mockComponentClasses.add(cls);
        Assertions.assertThrows(RuntimeException.class, () -> autoRoutingScanExtension.afterInjector(injector));
    }

    @ParameterizedTest
    @ValueSource(classes = {
            PreHandlerReturnsVoid.class,
            PreHandlerReturnsBoolean.class,
            PreHandlerReturnsbooleanType.class,
            PreHandlerReturnsCompletableFutureVoid.class,
            PreHandlerReturnsCompletableFutureBoolean.class
    })
    void testSuccessWhenPreHandlerReturnTypeIsValid(Class<?> cls) {
        mockComponentClasses.add(cls);
        autoRoutingScanExtension.afterInjector(injector);
    }
}
