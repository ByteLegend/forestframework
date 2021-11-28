package io.forestframework.core.http;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpFrame;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.StreamPriority;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
public class DefaultHttpResponse implements HttpResponse {
    private final io.vertx.core.http.HttpServerResponse delegate;
    private final List<Buffer> bodyBuffers = new ArrayList<>();

    public DefaultHttpResponse(io.vertx.core.http.HttpServerResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    public HttpResponse exceptionHandler(Handler<Throwable> handler) {
        delegate.exceptionHandler(handler);
        return this;
    }

    @Override
    public HttpResponse setWriteQueueMaxSize(int maxSize) {
        delegate.setWriteQueueMaxSize(maxSize);
        return this;
    }

    @Override
    public HttpResponse drainHandler(Handler<Void> handler) {
        delegate.drainHandler(handler);
        return this;
    }

    @Override
    public int getStatusCode() {
        return delegate.getStatusCode();
    }

    @Override
    public HttpResponse setStatusCode(int statusCode) {
        delegate.setStatusCode(statusCode);
        return this;
    }

    @Override
    public String getStatusMessage() {
        return delegate.getStatusMessage();
    }

    @Override
    public HttpResponse setStatusMessage(String statusMessage) {
        delegate.setStatusMessage(statusMessage);
        return this;
    }

    @Override
    public HttpResponse setChunked(boolean chunked) {
        delegate.setChunked(chunked);
        return this;
    }

    @Override
    public boolean isChunked() {
        return delegate.isChunked();
    }

    @Override
    public MultiMap headers() {
        return delegate.headers();
    }

    @Override
    public HttpResponse putHeader(String name, String value) {
        delegate.putHeader(name, value);
        return this;
    }

    @Override
    public HttpResponse putHeader(CharSequence name, CharSequence value) {
        delegate.putHeader(name, value);
        return this;
    }

    @Override
    public HttpResponse putHeader(String name, Iterable<String> values) {
        delegate.putHeader(name, values);
        return this;
    }

    @Override
    public HttpResponse putHeader(CharSequence name, Iterable<CharSequence> values) {
        delegate.putHeader(name, values);
        return this;
    }

    @Override
    public MultiMap trailers() {
        return delegate.trailers();
    }

    @Override
    public HttpResponse putTrailer(String name, String value) {
        delegate.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpResponse putTrailer(CharSequence name, CharSequence value) {
        delegate.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpResponse putTrailer(String name, Iterable<String> values) {
        delegate.putTrailer(name, values);
        return this;
    }

    @Override
    public HttpResponse putTrailer(CharSequence name, Iterable<CharSequence> value) {
        delegate.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpResponse closeHandler(Handler<Void> handler) {
        delegate.closeHandler(handler);
        return this;
    }

    @Override
    public HttpResponse endHandler(Handler<Void> handler) {
        delegate.endHandler(handler);
        return this;
    }

    @Override
    public HttpResponse writeContinue() {
        delegate.writeContinue();
        return this;
    }

    @Override
    public Future<Void> sendFile(String filename) {
        return delegate.sendFile(filename);
    }

    @Override
    public Future<Void> sendFile(String filename, long offset) {
        return delegate.sendFile(filename, offset);
    }

    @Override
    public Future<Void> sendFile(String filename, long offset, long length) {
        return delegate.sendFile(filename, offset, length);
    }

    @Override
    public HttpResponse sendFile(String filename, Handler<AsyncResult<Void>> resultHandler) {
        delegate.sendFile(filename, resultHandler);
        return this;
    }

    @Override
    public HttpResponse sendFile(String filename, long offset, Handler<AsyncResult<Void>> resultHandler) {
        delegate.sendFile(filename, offset, resultHandler);
        return this;
    }

    @Override
    public HttpResponse sendFile(String filename, long offset, long length, Handler<AsyncResult<Void>> resultHandler) {
        delegate.sendFile(filename, offset, length, resultHandler);
        return this;
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public boolean ended() {
        return delegate.ended();
    }

    @Override
    public boolean closed() {
        return delegate.closed();
    }

    @Override
    public boolean headWritten() {
        return delegate.headWritten();
    }

    @Override
    public HttpResponse headersEndHandler(Handler<Void> handler) {
        delegate.headersEndHandler(handler);
        return this;
    }

    @Override
    public HttpResponse bodyEndHandler(Handler<Void> handler) {
        delegate.bodyEndHandler(handler);
        return this;
    }

    @Override
    public long bytesWritten() {
        return delegate.bytesWritten();
    }

    @Override
    public int streamId() {
        return delegate.streamId();
    }

    @Override
    public Future<io.vertx.core.http.HttpServerResponse> push(HttpMethod method, String host, String path, MultiMap headers) {
        return delegate.push(method, host, path, headers);
    }

    @Override
    public boolean reset() {
        return delegate.reset();
    }

    @Override
    public boolean reset(long code) {
        return delegate.reset(code);
    }

    @Override
    public HttpResponse writeCustomFrame(int type, int flags, Buffer payload) {
        delegate.writeCustomFrame(type, flags, payload);
        return this;
    }

    @Override
    public HttpResponse writeCustomFrame(HttpFrame frame) {
        delegate.writeCustomFrame(frame);
        return this;
    }

    @Override
    public HttpResponse setStreamPriority(StreamPriority streamPriority) {
        delegate.setStreamPriority(streamPriority);
        return this;
    }

    @Override
    public HttpResponse addCookie(Cookie cookie) {
        delegate.addCookie(cookie);
        return this;
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
    public Set<Cookie> removeCookies(String name, boolean invalidate) {
        return delegate.removeCookies(name, invalidate);
    }

    @Override
    public Cookie removeCookie(String name, String domain, String path, boolean invalidate) {
        return delegate.removeCookie(name, domain, path, invalidate);
    }

    @Override
    public boolean writeQueueFull() {
        return delegate.writeQueueFull();
    }

    @Override
    public Future<Void> write(Buffer data) {
        return flush().compose(__ -> delegate.write(data));
    }

    @Override
    public void write(Buffer data, Handler<AsyncResult<Void>> handler) {
        flush().onComplete(result -> {
            if (result.succeeded()) {
                write(data, handler);
            } else {
                handler.handle(result);
            }
        });
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
        delegate.end(handler);
    }

    @Override
    public Future<Void> end(String chunk) {
        return end(Buffer.buffer(chunk));
    }

    @Override
    public void end(String chunk, Handler<AsyncResult<Void>> handler) {
        end(Buffer.buffer(chunk), handler);
    }

    @Override
    public Future<Void> end(String chunk, String enc) {
        throw new UnsupportedOperationException("We don't support non-UTF8 encoding currently, please use end(String chunk) instead.");
    }

    @Override
    public void end(String chunk, String enc, Handler<AsyncResult<Void>> handler) {
        throw new UnsupportedOperationException("We don't support non-UTF8 encoding currently, please use end(chunk, handler) instead.");
    }

    @Override
    public Future<Void> end(Buffer chunk) {
        return delegate.end(mergeBuffers(chunk));
    }

    private synchronized Buffer mergeBuffers(Buffer chunk) {
        if (bodyBuffers.isEmpty()) {
            return chunk;
        }
        Buffer ret = bodyBuffers.get(0);
        for (int i = 1; i < bodyBuffers.size(); ++i) {
            ret = ret.appendBuffer(bodyBuffers.get(i));
        }
        bodyBuffers.clear();
        return ret.appendBuffer(chunk);
    }

    @Override
    public void end(Buffer chunk, Handler<AsyncResult<Void>> handler) {
        delegate.end(mergeBuffers(chunk), handler);
    }

    @Override
    public Future<Void> end() {
        return end(Buffer.buffer());
    }

    @Override
    public Future<Void> write(String chunk, String enc) {
        throw new UnsupportedOperationException("We only support UTF8 encoding currently, please use write(chunk, enc) instead.");
    }

    @Override
    public void write(String chunk, String enc, Handler<AsyncResult<Void>> handler) {
        throw new UnsupportedOperationException("We only support UTF8 encoding currently, please use write(chunk, enc, handler) instead.");
    }

    @Override
    public Future<Void> write(String chunk) {
        return flush().compose(__ -> delegate.write(chunk));
    }

    @Override
    public void write(String chunk, Handler<AsyncResult<Void>> handler) {
        flush().onComplete(result -> {
            if (result.succeeded()) {
                delegate.write(chunk, handler);
            } else {
                handler.handle(result);
            }
        });
    }

    @Override
    public HttpResponse writeLater(String chunk) {
        bodyBuffers.add(Buffer.buffer(chunk));
        return this;
    }

    @Override
    public HttpResponse writeLater(Buffer chunk) {
        bodyBuffers.add(chunk);
        return this;
    }

    /**
     * Flush all current buffers in memory
     */
    public synchronized Future<Void> flush() {
        Future<Void> ret = Future.succeededFuture();
        for (Buffer buffer : bodyBuffers) {
            ret = ret.compose(__ -> delegate.write(buffer));
        }
        bodyBuffers.clear();
        return ret;
    }
}
