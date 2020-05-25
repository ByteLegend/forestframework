package org.forestframework;

public class HttpException extends RuntimeException {
    public static HttpException notFound() {
        return new HttpException();
    }
    public static HttpException notAcceptable() {
        return new HttpException();
    }
}
