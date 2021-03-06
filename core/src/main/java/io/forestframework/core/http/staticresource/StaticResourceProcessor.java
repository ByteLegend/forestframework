package io.forestframework.core.http.staticresource;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.forestframework.core.http.HttpContext;
import io.forestframework.core.http.HttpResponse;
import io.forestframework.core.http.HttpStatusCode;
import io.forestframework.core.http.result.RoutingResultProcessor;
import io.forestframework.core.http.routing.Routing;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.ParsedHeaderValues;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolve the resource name returned by a handler method, then send the resolved file via {@link io.vertx.core.http.HttpServerResponse}.
 * <p>
 * If the returned resource name is absolute, it will be sent directly; otherwise, a relative path will be interpreted as path relative to cwd or classpath entry.
 * <p>
 * Given a resource name "relative/path/to/resource.txt", for directory classpath entry "file:///home/my/resources",
 * the resource file "file:///home/my/resources/relative/path/to/resource.txt" will be located and sent; for jar classpath entry
 * "file:///home/lib/my.jar", the "relative/path/to/resource.txt" inside that jar will be located and sent.
 */
@Singleton
@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
public class StaticResourceProcessor implements RoutingResultProcessor {
    private final ForkedStaticHandlerImpl staticHandler = new ForkedStaticHandlerImpl();
    private final Vertx vertx;

    @Inject
    public StaticResourceProcessor(Vertx vertx) {
        this.vertx = vertx;
    }

    public void setMaxAgeSeconds(long maxAgeSeconds) {
        staticHandler.setMaxAgeSeconds(maxAgeSeconds);
    }

    public void setCachingEnabled(boolean enabled) {
        staticHandler.setCachingEnabled(enabled);
    }

    @Override
    public Object processResponse(HttpContext context, Routing routing, Object returnValue) {
        String path = returnValue.toString();

        if (path.isEmpty()) {
            send404(context.response());
            return returnValue;
        }

        EndAwareRoutingContextDecorator endAwareRoutingContext = new EndAwareRoutingContextDecorator(vertx, context);
        staticHandler.sendStatic(endAwareRoutingContext, vertx.fileSystem(), path);
        return endAwareRoutingContext.getFuture();
    }

    private static void send404(HttpResponse response) {
        response.setStatusCode(HttpStatusCode.NOT_FOUND.getCode());
        response.writeLater(HttpStatusCode.NOT_FOUND.name());
    }

    public static class EndAwareRoutingContextDecorator implements RoutingContext {
        private final Vertx vertx;
        private final HttpServerRequest request;
        private final HttpServerResponse response;
        private final Promise<Void> promise = Promise.promise();

        public EndAwareRoutingContextDecorator(Vertx vertx, HttpContext context) {
            this.vertx = vertx;
            this.request = context.request();
            this.response = context.response();
        }

        public Future<Void> getFuture() {
            return promise.future();
        }

        @Override
        public Future<Void> end(String chunk) {
            promise.complete();
            return response.end(chunk);
        }

        @Override
        public Future<Void> end(Buffer buffer) {
            promise.complete();
            return response.end(buffer);
        }

        @Override
        public Future<Void> end() {
            promise.complete();
            return response.end();
        }

        @Override
        public HttpServerRequest request() {
            return request;
        }

        @Override
        public HttpServerResponse response() {
            return response;
        }

        @Override
        public void next() {
            // Resource not found
            send404((HttpResponse) response());
            promise.complete();
        }

        @Override
        public Vertx vertx() {
            return vertx;
        }

        @Override
        public void fail(int statusCode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void fail(Throwable throwable) {
            throw new UnsupportedOperationException(throwable);
        }

        @Override
        public void fail(int statusCode, Throwable throwable) {
            throw new UnsupportedOperationException(throwable);
        }

        @Override
        public RoutingContext put(String key, Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T get(String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T get(String key, T defaultValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T remove(String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Object> data() {
            throw new UnsupportedOperationException();
        }


        @Override
        public String mountPoint() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Route currentRoute() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String normalizedPath() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Cookie getCookie(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RoutingContext addCookie(Cookie cookie) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Cookie removeCookie(String name, boolean invalidate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int cookieCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Cookie> cookieMap() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getBodyAsString() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getBodyAsString(String encoding) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JsonObject getBodyAsJson(int maxAllowedLength) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JsonArray getBodyAsJsonArray(int maxAllowedLength) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JsonObject getBodyAsJson() {
            throw new UnsupportedOperationException();
        }

        @Override
        public JsonArray getBodyAsJsonArray() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Buffer getBody() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<FileUpload> fileUploads() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Session session() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isSessionAccessed() {
            throw new UnsupportedOperationException();
        }

        @Override
        public User user() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Throwable failure() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int statusCode() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getAcceptableContentType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ParsedHeaderValues parsedHeaders() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int addHeadersEndHandler(Handler<Void> handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeHeadersEndHandler(int handlerID) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int addBodyEndHandler(Handler<Void> handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeBodyEndHandler(int handlerID) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int addEndHandler(Handler<AsyncResult<Void>> handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeEndHandler(int handlerID) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean failed() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setBody(Buffer body) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSession(Session session) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setUser(User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clearUser() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setAcceptableContentType(String contentType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void reroute(HttpMethod method, String path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, String> pathParams() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String pathParam(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MultiMap queryParams() {
            throw new UnsupportedOperationException();
        }

        @Override
        public MultiMap queryParams(Charset encoding) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> queryParam(String name) {
            throw new UnsupportedOperationException();
        }
    }
}

