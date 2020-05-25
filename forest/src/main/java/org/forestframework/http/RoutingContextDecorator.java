package org.forestframework.http;

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

public class RoutingContextDecorator implements RoutingContext {

    private final RoutingContext delegate;
    private boolean rerouteInvoked;

    public RoutingContextDecorator(RoutingContext delegate) {
        this.delegate = delegate;
    }

    public HttpServerRequest request() {
        return delegate.request();
    }

    public HttpServerResponse response() {
        return delegate.response();
    }

    public void next() {
        delegate.next();
    }

    public void fail(int statusCode) {
        delegate.fail(statusCode);
    }

    public void fail(Throwable throwable) {
        delegate.fail(throwable);
    }

    public void fail(int statusCode, Throwable throwable) {
        delegate.fail(statusCode, throwable);
    }

    public RoutingContext put(String key, Object obj) {
        return delegate.put(key, obj);
    }

    public <T> T get(String key) {
        return delegate.get(key);
    }

    public <T> T remove(String key) {
        return delegate.remove(key);
    }

    public Map<String, Object> data() {
        return delegate.data();
    }

    public Vertx vertx() {
        return delegate.vertx();
    }

    public String mountPoint() {
        return delegate.mountPoint();
    }

    public Route currentRoute() {
        return delegate.currentRoute();
    }

    public String normalisedPath() {
        return delegate.normalisedPath();
    }

    public Cookie getCookie(String name) {
        return delegate.getCookie(name);
    }

    public RoutingContext addCookie(Cookie cookie) {
        return delegate.addCookie(cookie);
    }

    public Cookie removeCookie(String name) {
        return delegate.removeCookie(name);
    }

    public Cookie removeCookie(String name, boolean invalidate) {
        return delegate.removeCookie(name, invalidate);
    }

    public int cookieCount() {
        return delegate.cookieCount();
    }

    public Map<String, Cookie> cookieMap() {
        return delegate.cookieMap();
    }

    public String getBodyAsString() {
        return delegate.getBodyAsString();
    }

    public String getBodyAsString(String encoding) {
        return delegate.getBodyAsString(encoding);
    }

    public JsonObject getBodyAsJson() {
        return delegate.getBodyAsJson();
    }

    public JsonArray getBodyAsJsonArray() {
        return delegate.getBodyAsJsonArray();
    }

    public Buffer getBody() {
        return delegate.getBody();
    }

    public Set<FileUpload> fileUploads() {
        return delegate.fileUploads();
    }

    public Session session() {
        return delegate.session();
    }

    public User user() {
        return delegate.user();
    }

    public Throwable failure() {
        return delegate.failure();
    }

    public int statusCode() {
        return delegate.statusCode();
    }

    public String getAcceptableContentType() {
        return delegate.getAcceptableContentType();
    }

    public ParsedHeaderValues parsedHeaders() {
        return delegate.parsedHeaders();
    }

    public int addHeadersEndHandler(Handler<Void> handler) {
        return delegate.addHeadersEndHandler(handler);
    }

    public boolean removeHeadersEndHandler(int handlerID) {
        return delegate.removeHeadersEndHandler(handlerID);
    }

    public int addBodyEndHandler(Handler<Void> handler) {
        return delegate.addBodyEndHandler(handler);
    }

    public boolean removeBodyEndHandler(int handlerID) {
        return delegate.removeBodyEndHandler(handlerID);
    }

    public boolean failed() {
        return delegate.failed();
    }

    public void setBody(Buffer body) {
        delegate.setBody(body);
    }

    public void setSession(Session session) {
        delegate.setSession(session);
    }

    public void setUser(User user) {
        delegate.setUser(user);
    }

    public void clearUser() {
        delegate.clearUser();
    }

    public void setAcceptableContentType(String contentType) {
        delegate.setAcceptableContentType(contentType);
    }

    public boolean isRerouteInvoked() {
        return rerouteInvoked;
    }

    public void reroute(String path) {
        rerouteInvoked = true;
        delegate.reroute(path);
    }

    public void reroute(HttpMethod method, String path) {
        delegate.reroute(method, path);
    }

    public List<LanguageHeader> acceptableLanguages() {
        return delegate.acceptableLanguages();
    }

    public LanguageHeader preferredLanguage() {
        return delegate.preferredLanguage();
    }

    public Map<String, String> pathParams() {
        return delegate.pathParams();
    }

    public String pathParam(String name) {
        return delegate.pathParam(name);
    }

    public MultiMap queryParams() {
        return delegate.queryParams();
    }

    public List<String> queryParam(String name) {
        return delegate.queryParam(name);
    }

    public RoutingContext attachment(String filename) {
        return delegate.attachment(filename);
    }

    public Future<Void> redirect(String url) {
        return delegate.redirect(url);
    }

    public Future<Void> json(Object json) {
        return delegate.json(json);
    }

    public boolean is(String type) {
        return delegate.is(type);
    }

    public boolean isFresh() {
        return delegate.isFresh();
    }

    public RoutingContext etag(String etag) {
        return delegate.etag(etag);
    }

    public RoutingContext lastModified(Instant instant) {
        return delegate.lastModified(instant);
    }

    public RoutingContext lastModified(String instant) {
        return delegate.lastModified(instant);
    }

    public Future<Void> end(String chunk) {
        return delegate.end(chunk);
    }

    public Future<Void> end(Buffer buffer) {
        return delegate.end(buffer);
    }

    public Future<Void> end() {
        return delegate.end();
    }
}
