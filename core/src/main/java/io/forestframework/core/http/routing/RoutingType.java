package io.forestframework.core.http.routing;

import org.apiguardian.api.API;

/**
 * RoutingType provides interceptor-like mechanism. You can think of them as:
 *
 * <pre>
 *     try {
 *         PreHandler1;
 *         if(PreHandler1 returns true)
 *             PreHandler2;
 *         if(PreHandler2 returns true)
 *             PreHandler3;
 *         // ... more pre-handlers
 *
 *         Handler1;
 *         Handler2;
 *         Handler3;
 *         // ... more handlers
 *     } catch(HTTP status code) {
 *         errorHandler of specific HTTP status code;
 *     } finally {
 *         PostHandler1;
 *         PostHandler2;
 *         PostHandler3;
 *         // ... more post-handlers
 *     }
 * </pre>
 *
 * The invocation order is defined and configued in {@link RoutingManager}
 */

@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public enum RoutingType {
    /**
     * A handler running before the main handler, similar to interceptors.
     * A pre-handler should return boolean/Future[Boolean] which indicates if the routing process should continue.
     * When pre-handler returns false or throws exceptions, all following pre-handlers and handlers are ignored,
     * but after-handler will still be invoked. It can also return void, which has the same effect as returning true.
     *
     * You can use {@link io.forestframework.core.http.param.RoutingParameterResolver}s on pre-handlers, but not
     * {@link io.forestframework.core.http.result.RoutingResultProcessor}.
     */
    PRE_HANDLER,
    /**
     * The main route which handles {@link io.vertx.core.http.HttpServerRequest} and generates {@link io.vertx.core.http.HttpServerResponse}.
     *
     * You can freely use {@link io.forestframework.core.http.param.RoutingParameterResolver} and
     * {@link io.forestframework.core.http.result.RoutingResultProcessor} on handlers.
     */
    HANDLER,

    ERROR_HANDLER,
    /**
     * An after-handler is like a finally block in Java programming language. It should return void and never throw exceptions.
     *
     * You can use {@link io.forestframework.core.http.param.RoutingParameterResolver}s on pre-handlers, but not
     * {@link io.forestframework.core.http.result.RoutingResultProcessor}. Specially, you can inject exceptions throws by pre-handlers and handlers
     * as parameters of after-handlers. See {}.
     */
    POST_HANDLER,

    ON_WEB_SOCKET_OPEN,
    ON_WEB_SOCKET_MESSAGE,
    ON_WEB_SOCKET_CLOSE,
    ON_WEB_SOCKET_ERROR,

    // See https://vertx.io/docs/vertx-web/java/#_sockjs
//    SOCK_JS,

    BRIDGE
}
