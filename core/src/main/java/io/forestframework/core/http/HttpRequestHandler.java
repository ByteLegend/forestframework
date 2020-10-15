package io.forestframework.core.http;

import io.forestframework.core.injector.DefaultImplementedBy;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.apiguardian.api.API;

/**
 * A handler which can process {@link HttpRequest}s and generate {@link io.vertx.core.http.HttpServerResponse}s.
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
@DefaultImplementedBy(DefaultHttpRequestDispatcher.class)
public interface HttpRequestHandler extends Handler<HttpServerRequest> {
}

