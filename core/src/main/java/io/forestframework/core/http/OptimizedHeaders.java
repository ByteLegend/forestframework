package io.forestframework.core.http;

import io.vertx.core.http.HttpHeaders;
import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public class OptimizedHeaders {
    public static final CharSequence HEADER_CONTENT_TYPE = HttpHeaders.createOptimized("content-type");
    public static final CharSequence CONTENT_TYPE_TEXT_PLAIN = HttpHeaders.createOptimized("text/plain");
}
