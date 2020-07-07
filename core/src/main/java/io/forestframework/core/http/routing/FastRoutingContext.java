package io.forestframework.core.http.routing;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FastRoutingContext implements RoutingContext {
    private final HttpServerRequest request;
    private final HttpServerResponse response;
    private final ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();
    private final Vertx vertx;

    public FastRoutingContext(Vertx vertx, HttpServerRequest request) {
        this.vertx = vertx;
        this.request = request;
        this.response = request.response();
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
        data.put(key, obj);
        return this;
    }

    @Override
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    @Override
    public <T> T remove(String key) {
        return (T) data.remove(key);
    }

    @Override
    public Map<String, Object> data() {
        return data;
    }

    @Override
    public Vertx vertx() {
        return vertx;
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
