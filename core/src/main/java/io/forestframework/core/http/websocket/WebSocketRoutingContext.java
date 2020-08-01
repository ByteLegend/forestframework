package io.forestframework.core.http.websocket;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.ParsedHeaderValues;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import org.apiguardian.api.API;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a fake {@link RoutingContext} implementation so that we can use
 * {@link io.forestframework.core.http.param.RoutingParameterResolver}s in WebSocket handlers.
 */
@API(status = API.Status.INTERNAL, since = "0.1")
public class WebSocketRoutingContext implements RoutingContext {
    private final ServerWebSocket webSocket;

    public WebSocketRoutingContext(ServerWebSocket webSocket) {
        this.webSocket = webSocket;
    }

    @Override
    public HttpServerRequest request() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse response() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void next() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fail(int statusCode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fail(Throwable throwable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fail(int statusCode, Throwable throwable) {
        throw new UnsupportedOperationException();
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
    public <T> T remove(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> data() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vertx vertx() {
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
    public List<String> queryParam(String name) {
        throw new UnsupportedOperationException();
    }
}
