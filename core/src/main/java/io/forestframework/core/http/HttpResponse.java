package io.forestframework.core.http;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerResponse;

/**
 * Provides extra forestframework-specific methods.
 */
public interface HttpResponse extends HttpServerResponse {
    /**
     * Write a {@link String} to the response body buffer (but not flush it), with UTF-8 encoding.
     *
     * @param chunk the string to write
     */
    HttpResponse writeLater(String chunk);

    /**
     * Write a {@link String} to the response body buffer (but not flush it), with UTF-8 encoding.
     *
     * @param chunk the data to write
     */
    HttpResponse writeLater(Buffer chunk);

    @Override
    HttpResponse setStatusCode(int statusCode);

    @Override
    HttpResponse exceptionHandler(Handler<Throwable> handler);

    @Override
    HttpResponse setWriteQueueMaxSize(int maxSize);

    @Override
    HttpResponse drainHandler(Handler<Void> handler);

    @Override
    HttpResponse setStatusMessage(String statusMessage);

    @Override
    HttpResponse setChunked(boolean chunked);

    @Override
    HttpResponse putHeader(String name, String value);

    @Override
    HttpResponse putHeader(CharSequence name, CharSequence value);

    @Override
    HttpResponse putHeader(String name, Iterable<String> values);

    @Override
    HttpResponse putHeader(CharSequence name, Iterable<CharSequence> values);

    @Override
    HttpResponse putTrailer(String name, String value);

    @Override
    HttpResponse putTrailer(CharSequence name, CharSequence value);

    @Override
    HttpResponse putTrailer(String name, Iterable<String> values);

    @Override
    HttpResponse putTrailer(CharSequence name, Iterable<CharSequence> value);

    @Override
    HttpResponse closeHandler(Handler<Void> handler);

    @Override
    HttpResponse endHandler(Handler<Void> handler);

    @Override
    HttpResponse writeContinue();

    @Override
    HttpResponse sendFile(String filename, long offset, long length, Handler<AsyncResult<Void>> resultHandler);

    @Override
    HttpResponse headersEndHandler(Handler<Void> handler);

    @Override
    HttpResponse bodyEndHandler(Handler<Void> handler);

    @Override
    HttpResponse writeCustomFrame(int type, int flags, Buffer payload);

    @Override
    HttpResponse addCookie(Cookie cookie);
}

