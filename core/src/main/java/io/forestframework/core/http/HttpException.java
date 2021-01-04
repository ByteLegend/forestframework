package io.forestframework.core.http;

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

    @Override
    public String getMessage() {
        return super.getMessage() == null ? code.toString() : super.getMessage();
    }

    public static HttpException notFound() {
        return new HttpException(HttpStatusCode.NOT_FOUND);
    }

    public static HttpException unauthorized() {
        return new HttpException(HttpStatusCode.UNAUTHORIZED);
    }
}
