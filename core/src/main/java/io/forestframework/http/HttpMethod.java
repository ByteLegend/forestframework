package io.forestframework.http;

public enum HttpMethod {
    /**
     * The RFC 2616 {@code OPTIONS} method, this instance is interned and uniquely used.
     */
    OPTIONS,

    /**
     * The RFC 2616 {@code GET} method, this instance is interned and uniquely used.
     */
     GET,

    /**
     * The RFC 2616 {@code HEAD} method, this instance is interned and uniquely used.
     */
     HEAD,

    /**
     * The {RFC 2616 @code POST} method, this instance is interned and uniquely used.
     */
     POST,

    /**
     * The RFC 2616 {@code PUT} method, this instance is interned and uniquely used.
     */
     PUT,

    /**
     * The RFC 2616 {@code DELETE} method, this instance is interned and uniquely used.
     */
     DELETE,

    /**
     * The RFC 2616 {@code TRACE} method, this instance is interned and uniquely used.
     */
     TRACE,

    /**
     * The RFC 2616 {@code CONNECT} method, this instance is interned and uniquely used.
     */
     CONNECT,

    /**
     * The RFC 5789 {@code PATCH} method, this instance is interned and uniquely used.
     */
     PATCH,

    /**
     * The RFC 2518/4918 {@code PROPFIND} method, this instance is interned and uniquely used.
     */
     PROPFIND,

    /**
     * The RFC 2518/4918 {@code PROPPATCH} method, this instance is interned and uniquely used.
     */
     PROPPATCH,

    /**
     * The RFC 2518/4918 {@code MKCOL} method, this instance is interned and uniquely used.
     */
     MKCOL,

    /**
     * The RFC 2518/4918 {@code COPY} method, this instance is interned and uniquely used.
     */
     COPY,

    /**
     * The RFC 2518/4918 {@code MOVE} method, this instance is interned and uniquely used.
     */
     MOVE,

    /**
     * The RFC 2518/4918 {@code LOCK} method, this instance is interned and uniquely used.
     */
     LOCK,

    /**
     * The RFC 2518/4918 {@code UNLOCK} method, this instance is interned and uniquely used.
     */
     UNLOCK,

    /**
     * The RFC 4791 {@code MKCALENDAR} method, this instance is interned and uniquely used.
     */
     MKCALENDAR,

    /**
     * The RFC 3253 {@code VERSION_CONTROL} method, this instance is interned and uniquely used.
     */
     VERSION_CONTROL,

    /**
     * The RFC 3253 {@code REPORT} method, this instance is interned and uniquely used.
     */
     REPORT,

    /**
     * The RFC 3253 {@code CHECKOUT} method, this instance is interned and uniquely used.
     */
     CHECKOUT,

    /**
     * The RFC 3253 {@code CHECKIN} method, this instance is interned and uniquely used.
     */
     CHECKIN,

    /**
     * The RFC 3253 {@code UNCHECKOUT} method, this instance is interned and uniquely used.
     */
     UNCHECKOUT,

    /**
     * The RFC 3253 {@code MKWORKSPACE} method, this instance is interned and uniquely used.
     */
     MKWORKSPACE,

    /**
     * The RFC 3253 {@code UPDATE} method, this instance is interned and uniquely used.
     */
     UPDATE,

    /**
     * The RFC 3253 {@code LABEL} method, this instance is interned and uniquely used.
     */
     LABEL,

    /**
     * The RFC 3253 {@code MERGE} method, this instance is interned and uniquely used.
     */
     MERGE,

    /**
     * The RFC 3253 {@code BASELINE_CONTROL} method, this instance is interned and uniquely used.
     */
     BASELINE_CONTROL,

    /**
     * The RFC 3253 {@code MKACTIVITY} method, this instance is interned and uniquely used.
     */
     MKACTIVITY,

    /**
     * The RFC 3648 {@code ORDERPATCH} method, this instance is interned and uniquely used.
     */
     ORDERPATCH,

    /**
     * The RFC 3744 {@code ACL} method, this instance is interned and uniquely used.
     */
     ACL,

    /**
     * The RFC 5323 {@code SEARCH} method, this instance is interned and uniquely used.
     */
     SEARCH;

     public io.vertx.core.http.HttpMethod toVertxHttpMethod() {
         return io.vertx.core.http.HttpMethod.valueOf(toString());
     }
}
