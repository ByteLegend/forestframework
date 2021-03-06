package io.forestframework.core.http;

import io.forestframework.core.http.routing.RoutingMatchResult;
import io.netty.handler.codec.DecoderResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
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
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.StreamPriority;
import io.vertx.core.http.impl.HttpServerRequestInternal;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.streams.Pipe;
import io.vertx.core.streams.WriteStream;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

public class DefaultHttpRequest implements HttpRequest {
    private final HttpServerRequestInternal delegate;
    private final HttpResponse response;
    private final RoutingMatchResult routingMatchResult;
    /**
     * This allows a body to be read multiple times, in multiple handlers.
     */
    private Future<Buffer> bodyCache;

    public DefaultHttpRequest(HttpServerRequestInternal delegate, RoutingMatchResult routingMatchResult) {
        this.delegate = delegate;
        this.routingMatchResult = routingMatchResult;
        this.response = new DefaultHttpResponse(delegate.response());
        // TODO this is unsafe
        // However, we need this to handle the situation in which HTTP request is read to the end
        // before `body()` is called.
        // Also, as said in `bodyHandler` documentation, it's unsafe to call it on a huge input
        bodyHandler(buffer -> bodyCache = Future.succeededFuture(buffer));
    }

    @SuppressWarnings("unchecked")
    public <T extends RoutingMatchResult> T getRoutingMatchResult() {
        return (T) routingMatchResult;
    }

    @Override
    public HttpRequest exceptionHandler(Handler<Throwable> handler) {
        delegate.exceptionHandler(handler);
        return this;
    }

    @Override
    public HttpRequest handler(Handler<Buffer> handler) {
        delegate.handler(handler);
        return this;
    }

    @Override
    public HttpRequest pause() {
        delegate.pause();
        return this;
    }

    @Override
    public HttpRequest resume() {
        delegate.resume();
        return this;
    }

    @Override
    public HttpRequest fetch(long amount) {
        delegate.fetch(amount);
        return this;
    }

    @Override
    public HttpRequest endHandler(Handler<Void> endHandler) {
        delegate.endHandler(endHandler);
        return this;
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
    public HttpResponse response() {
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
    public HttpRequest bodyHandler(Handler<Buffer> bodyHandler) {
        delegate.bodyHandler(bodyHandler);
        return this;
    }

    @Override
    public HttpRequest body(Handler<AsyncResult<Buffer>> handler) {
        delegate.body(handler);
        return this;
    }

    @Override
    public Future<Buffer> body() {
        if (bodyCache == null) {
            bodyCache = delegate.body();
        }
        return bodyCache;
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
        delegate.end(handler);
    }

    @Override
    public Future<Void> end() {
        return delegate.end();
    }

    @Override
    public void toNetSocket(Handler<AsyncResult<NetSocket>> handler) {
        delegate.toNetSocket(handler);
    }

    @Override
    public Future<NetSocket> toNetSocket() {
        return delegate.toNetSocket();
    }

    @Override
    public HttpRequest setExpectMultipart(boolean expect) {
        delegate.setExpectMultipart(expect);
        return this;
    }

    @Override
    public boolean isExpectMultipart() {
        return delegate.isExpectMultipart();
    }

    @Override
    public HttpRequest uploadHandler(Handler<HttpServerFileUpload> uploadHandler) {
        delegate.uploadHandler(uploadHandler);
        return this;
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
    public int streamId() {
        return delegate.streamId();
    }

    @Override
    public void toWebSocket(Handler<AsyncResult<ServerWebSocket>> handler) {
        delegate.toWebSocket(handler);
    }

    @Override
    public Future<ServerWebSocket> toWebSocket() {
        return delegate.toWebSocket();
    }

    @Override
    public boolean isEnded() {
        return delegate.isEnded();
    }

    @Override
    public HttpRequest customFrameHandler(Handler<HttpFrame> handler) {
        delegate.customFrameHandler(handler);
        return this;
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
    public HttpRequest streamPriorityHandler(Handler<StreamPriority> handler) {
        delegate.streamPriorityHandler(handler);
        return this;
    }

    @Override
    public DecoderResult decoderResult() {
        return delegate.decoderResult();
    }

    @Override
    public Cookie getCookie(String name) {
        return delegate.getCookie(name);
    }

    @Override
    public Cookie getCookie(String name, String domain, String path) {
        return delegate.getCookie(name, domain, path);
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
    public Set<Cookie> cookies(String name) {
        return delegate.cookies();
    }

    @Override
    public Set<Cookie> cookies() {
        return delegate.cookies();
    }

    @Override
    public HttpServerRequest routed(String route) {
        delegate.routed(route);
        return this;
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

    @Override
    public Context context() {
        return delegate.context();
    }

    @Override
    public Object metric() {
        return delegate.metric();
    }
}
