package io.forestframework.ext.core;

import io.forestframework.core.http.HttpStatusCode;

public class HttpException extends RuntimeException {
    private final HttpStatusCode code;

    public HttpException(HttpStatusCode code) {
        this(code, null);
    }

    public HttpException(HttpStatusCode code, String message) {
        super(message == null ? code.name() : message);
        this.code = code;
    }

    public HttpStatusCode getCode() {
        return code;
    }

    public static HttpException notFound() {
        return new HttpException(HttpStatusCode.NOT_FOUND);
    }

    public static HttpException unauthorized() {
        return new HttpException(HttpStatusCode.UNAUTHORIZED);
    }
}
