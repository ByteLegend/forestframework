package io.forestframework.core.http.routing;

import com.google.inject.ImplementedBy;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;

/**
 * A handler which can process {@link HttpServerRequest}s and generate {@link io.vertx.core.http.HttpServerResponse}s.
 */
@ImplementedBy(RequestHandlerChain.class)
public interface RequestHandler extends Handler<HttpServerRequest> {
}
