package io.forestframework.core.http;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
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
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.ParsedHeaderValues;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractRoutingContextDecorator implements RoutingContext {
    private final RoutingContext delegate;

    public AbstractRoutingContextDecorator(RoutingContext delegate) {
        this.delegate = delegate;
    }

    @Override
    public HttpServerRequest request() {
        return delegate.request();
    }

    @Override
    public HttpServerResponse response() {
        return delegate.response();
    }

    @Override
    public void next() {
        delegate.next();
    }

    @Override
    public void fail(int statusCode) {
        delegate.fail(statusCode);
    }

    @Override
    public void fail(Throwable throwable) {
        delegate.fail(throwable);
    }

    @Override
    public void fail(int statusCode, Throwable throwable) {
        delegate.fail(statusCode, throwable);
    }

    @Override
    public RoutingContext put(String key, Object obj) {
        return delegate.put(key, obj);
    }

    @Override
    public <T> T get(String key) {
        return delegate.get(key);
    }

    @Override
    public <T> T remove(String key) {
        return delegate.remove(key);
    }

    @Override
    public Map<String, Object> data() {
        return delegate.data();
    }

    @Override
    public Vertx vertx() {
        return delegate.vertx();
    }

    @Override
    public String mountPoint() {
        return delegate.mountPoint();
    }

    @Override
    public Route currentRoute() {
        return delegate.currentRoute();
    }

    @Override
    @SuppressWarnings("deprecated")
    public String normalisedPath() {
        return delegate.normalizedPath();
    }

    @Override
    public String normalizedPath() {
        return delegate.normalizedPath();
    }

    @Override
    public Cookie getCookie(String name) {
        return delegate.getCookie(name);
    }

    @Override
    public RoutingContext addCookie(Cookie cookie) {
        return delegate.addCookie(cookie);
    }

    @Override
    public Cookie removeCookie(String name) {
        return delegate.removeCookie(name);
    }

    @Override
    public Cookie removeCookie(String name, boolean invalidate) {
        return delegate.removeCookie(name, invalidate);
    }

    @Override
    public int cookieCount() {
        return delegate.cookieCount();
    }

    @Override
    public Map<String, Cookie> cookieMap() {
        return delegate.cookieMap();
    }

    @Override
    public String getBodyAsString() {
        return delegate.getBodyAsString();
    }

    @Override
    public String getBodyAsString(String encoding) {
        return delegate.getBodyAsString(encoding);
    }

    @Override
    public JsonObject getBodyAsJson() {
        return delegate.getBodyAsJson();
    }

    @Override
    public JsonArray getBodyAsJsonArray() {
        return delegate.getBodyAsJsonArray();
    }

    @Override
    public Buffer getBody() {
        return delegate.getBody();
    }

    @Override
    public Set<FileUpload> fileUploads() {
        return delegate.fileUploads();
    }

    @Override
    public Session session() {
        return delegate.session();
    }

    @Override
    public boolean isSessionAccessed() {
        return delegate.isSessionAccessed();
    }

    @Override
    public User user() {
        return delegate.user();
    }

    @Override
    public Throwable failure() {
        return delegate.failure();
    }

    @Override
    public int statusCode() {
        return delegate.statusCode();
    }

    @Override
    public String getAcceptableContentType() {
        return delegate.getAcceptableContentType();
    }

    @Override
    public ParsedHeaderValues parsedHeaders() {
        return delegate.parsedHeaders();
    }

    @Override
    public int addHeadersEndHandler(Handler<Void> handler) {
        return delegate.addHeadersEndHandler(handler);
    }

    @Override
    public boolean removeHeadersEndHandler(int handlerID) {
        return delegate.removeHeadersEndHandler(handlerID);
    }

    @Override
    public int addBodyEndHandler(Handler<Void> handler) {
        return delegate.addBodyEndHandler(handler);
    }

    @Override
    public boolean removeBodyEndHandler(int handlerID) {
        return delegate.removeBodyEndHandler(handlerID);
    }

    @Override
    public int addEndHandler(Handler<AsyncResult<Void>> handler) {
        return delegate.addEndHandler(handler);
    }

    @Override
    public boolean removeEndHandler(int handlerID) {
        return delegate.removeEndHandler(handlerID);
    }

    @Override
    public boolean failed() {
        return delegate.failed();
    }

    @Override
    public void setBody(Buffer body) {
        delegate.setBody(body);
    }

    @Override
    public void setSession(Session session) {
        delegate.setSession(session);
    }

    @Override
    public void setUser(User user) {
        delegate.setUser(user);
    }

    @Override
    public void clearUser() {
        delegate.clearUser();
    }

    @Override
    public void setAcceptableContentType(String contentType) {
        delegate.setAcceptableContentType(contentType);
    }

    @Override
    public void reroute(String path) {
        delegate.reroute(path);
    }

    @Override
    public void reroute(HttpMethod method, String path) {
        delegate.reroute(method, path);
    }

    @Override
    public List<LanguageHeader> acceptableLanguages() {
        return delegate.acceptableLanguages();
    }

    @Override
    public LanguageHeader preferredLanguage() {
        return delegate.preferredLanguage();
    }

    @Override
    public Map<String, String> pathParams() {
        return delegate.pathParams();
    }

    @Override
    public String pathParam(String name) {
        return delegate.pathParam(name);
    }

    @Override
    public MultiMap queryParams() {
        return delegate.queryParams();
    }

    @Override
    public List<String> queryParam(String name) {
        return delegate.queryParam(name);
    }

    @Override
    public RoutingContext attachment(String filename) {
        return delegate.attachment(filename);
    }

    @Override
    public Future<Void> redirect(String url) {
        return delegate.redirect(url);
    }

    @Override
    public Future<Void> json(Object json) {
        return delegate.json(json);
    }

    @Override
    public boolean is(String type) {
        return delegate.is(type);
    }

    @Override
    public boolean isFresh() {
        return delegate.isFresh();
    }

    @Override
    public RoutingContext etag(String etag) {
        return delegate.etag(etag);
    }

    @Override
    public RoutingContext lastModified(Instant instant) {
        return delegate.lastModified(instant);
    }

    @Override
    public RoutingContext lastModified(String instant) {
        return delegate.lastModified(instant);
    }

    @Override
    public Future<Void> end(String chunk) {
        return delegate.end(chunk);
    }

    @Override
    public Future<Void> end(Buffer buffer) {
        return delegate.end(buffer);
    }

    @Override
    public Future<Void> end() {
        return delegate.end();
    }
}
