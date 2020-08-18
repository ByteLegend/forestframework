package io.forestframework.core.http;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
public interface PlainHttpContext extends WebContext {
    HttpServerRequest request();

    HttpServerResponse response();

    /**
     * Borrowed from {@link io.vertx.ext.web.RoutingContext}
     *
     * Perform a 302 redirect to {@code url}. If a custom 3xx code is already defined, then that
     * one will be preferred.
     * <p/>
     * The string "back" is special-cased
     * to provide Referrer support, when Referrer
     * is not present "/" is used.
     * <p/>
     * Examples:
     * <p/>
     * redirect('back');
     * redirect('/login');
     * redirect('http://google.com');
     *
     * @param url the target url
     */
    default Future<Void> redirect(String url) {
        // location
        if ("back".equals(url)) {
            url = request().getHeader(HttpHeaders.REFERER);
            if (url == null) {
                url = "/";
            }
        }

        response()
                .putHeader(HttpHeaders.LOCATION, url);

        // status
        int status = response().getStatusCode();

        if (status < 300 || status >= 400) {
            // if a custom code is in use that will be
            // respected
            response().setStatusCode(302);
        }

        return response()
                .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset=utf-8")
                .end("Redirecting to " + url + ".");
    }
}
