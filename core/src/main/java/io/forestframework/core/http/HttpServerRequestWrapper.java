package io.forestframework.core.http;

import io.forestframework.core.http.routing.RoutingMatchResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpFrame;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerFileUpload;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.StreamPriority;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.streams.Pipe;
import io.vertx.core.streams.WriteStream;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;
import java.util.Map;

public class HttpServerRequestWrapper implements HttpServerRequest {
    private final HttpServerRequest delegate;
    private final HttpServerResponse response;
    private final RoutingMatchResult routingMatchResult;

    public HttpServerRequestWrapper(HttpServerRequest delegate, RoutingMatchResult routingMatchResult) {
        this(delegate, routingMatchResult, false);
    }

    public HttpServerRequestWrapper(HttpServerRequest delegate, RoutingMatchResult routingMatchResult, boolean forbidEnd) {
        this.delegate = delegate;
        this.response = forbidEnd ? new EndForbiddenHttpServerResponseWrapper(delegate.response()) : delegate.response();
        this.routingMatchResult = routingMatchResult;
    }

    @SuppressWarnings("unchecked")
    public <T extends RoutingMatchResult> T getRoutingMatchResult() {
        return (T) routingMatchResult;
    }

    @Override
    public HttpServerRequest exceptionHandler(Handler<Throwable> handler) {
        return delegate.exceptionHandler(handler);
    }

    @Override
    public HttpServerRequest handler(Handler<Buffer> handler) {
        return delegate.handler(handler);
    }

    @Override
    public HttpServerRequest pause() {
        return delegate.pause();
    }

    @Override
    public HttpServerRequest resume() {
        return delegate.resume();
    }

    @Override
    public HttpServerRequest fetch(long amount) {
        return delegate.fetch(amount);
    }

    @Override
    public HttpServerRequest endHandler(Handler<Void> endHandler) {
        return delegate.endHandler(endHandler);
    }

    @Override
    public HttpVersion version() {
        return delegate.version();
    }

    @Override
    public HttpMethod method() {
        return delegate.method();
    }

    @Override
    public boolean isSSL() {
        return delegate.isSSL();
    }

    @Override

    public String scheme() {
        return delegate.scheme();
    }

    @Override
    public String uri() {
        return delegate.uri();
    }

    @Override

    public String path() {
        return delegate.path();
    }

    @Override

    public String query() {
        return delegate.query();
    }

    @Override

    public String host() {
        return delegate.host();
    }

    @Override
    public long bytesRead() {
        return delegate.bytesRead();
    }

    @Override
    public HttpServerResponse response() {
        return response;
    }

    @Override
    public MultiMap headers() {
        return delegate.headers();
    }

    @Override

    public String getHeader(String headerName) {
        return delegate.getHeader(headerName);
    }

    @Override
    public String getHeader(CharSequence headerName) {
        return delegate.getHeader(headerName);
    }

    @Override
    public MultiMap params() {
        return delegate.params();
    }

    @Override

    public String getParam(String paramName) {
        return delegate.getParam(paramName);
    }

    @Override
    public SocketAddress remoteAddress() {
        return delegate.remoteAddress();
    }

    @Override
    public SocketAddress localAddress() {
        return delegate.localAddress();
    }

    @Override
    public SSLSession sslSession() {
        return delegate.sslSession();
    }

    @Override
    public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
        return delegate.peerCertificateChain();
    }

    @Override
    public String absoluteURI() {
        return delegate.absoluteURI();
    }

    @Override
    public HttpServerRequest bodyHandler(Handler<Buffer> bodyHandler) {
        return delegate.bodyHandler(bodyHandler);
    }

    @Override
    public HttpServerRequest body(Handler<AsyncResult<Buffer>> handler) {
        return delegate.body(handler);
    }

    @Override
    public Future<Buffer> body() {
        return delegate.body();
    }

    @Override
    public NetSocket netSocket() {
        return delegate.netSocket();
    }

    @Override
    public HttpServerRequest setExpectMultipart(boolean expect) {
        return delegate.setExpectMultipart(expect);
    }

    @Override
    public boolean isExpectMultipart() {
        return delegate.isExpectMultipart();
    }

    @Override
    public HttpServerRequest uploadHandler(Handler<HttpServerFileUpload> uploadHandler) {
        return delegate.uploadHandler(uploadHandler);
    }

    @Override
    public MultiMap formAttributes() {
        return delegate.formAttributes();
    }

    @Override

    public String getFormAttribute(String attributeName) {
        return delegate.getFormAttribute(attributeName);
    }

    @Override
    public ServerWebSocket upgrade() {
        return delegate.upgrade();
    }

    @Override
    public boolean isEnded() {
        return delegate.isEnded();
    }

    @Override
    public HttpServerRequest customFrameHandler(Handler<HttpFrame> handler) {
        return delegate.customFrameHandler(handler);
    }

    @Override
    public HttpConnection connection() {
        return delegate.connection();
    }

    @Override
    public StreamPriority streamPriority() {
        return delegate.streamPriority();
    }

    @Override
    public HttpServerRequest streamPriorityHandler(Handler<StreamPriority> handler) {
        return delegate.streamPriorityHandler(handler);
    }

    @Override
    public Cookie getCookie(String name) {
        return delegate.getCookie(name);
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
    public Pipe<Buffer> pipe() {
        return delegate.pipe();
    }

    @Override
    public Future<Void> pipeTo(WriteStream<Buffer> dst) {
        return delegate.pipeTo(dst);
    }

    @Override
    public void pipeTo(WriteStream<Buffer> dst, Handler<AsyncResult<Void>> handler) {
        delegate.pipeTo(dst, handler);
    }
}
