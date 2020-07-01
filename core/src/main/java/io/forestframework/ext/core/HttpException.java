package io.forestframework.ext.core;

import io.forestframework.core.http.HttpStatusCode;

public class HttpException extends RuntimeException {
    private HttpStatusCode code;

    public HttpException(HttpStatusCode code) {
        this.code = code;
    }

    public HttpStatusCode getCode() {
        return code;
    }
}
