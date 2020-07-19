package io.forestframework.core.http;

import io.vertx.core.http.HttpHeaders;
import org.apiguardian.api.API;

import static com.google.common.net.MediaType.CSS_UTF_8;
import static com.google.common.net.MediaType.HTML_UTF_8;
import static com.google.common.net.MediaType.ICO;
import static com.google.common.net.MediaType.JAVASCRIPT_UTF_8;
import static com.google.common.net.MediaType.JPEG;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8;
import static com.google.common.net.MediaType.PNG;

@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public class OptimizedHeaders {
    public static final CharSequence HEADER_CONTENT_TYPE = HttpHeaders.createOptimized("content-type");
    public static final CharSequence HEADER_ACCEPT = HttpHeaders.createOptimized("accept");
    public static final CharSequence CONTENT_TYPE_TEXT_PLAIN = HttpHeaders.createOptimized(PLAIN_TEXT_UTF_8.toString());
    public static final CharSequence CONTENT_TYPE_TEXT_HTML = HttpHeaders.createOptimized(HTML_UTF_8.toString());
    public static final CharSequence CONTENT_TYPE_APPLICATION_JSON = HttpHeaders.createOptimized(JSON_UTF_8.toString());
    public static final CharSequence CONTENT_TYPE_APPLICATION_JAVASCRIPT = HttpHeaders.createOptimized(JAVASCRIPT_UTF_8.toString());
    public static final CharSequence CONTENT_TYPE_TEXT_CSS = HttpHeaders.createOptimized(CSS_UTF_8.toString());
    public static final CharSequence CONTENT_TYPE_IMAGE_JPEG = HttpHeaders.createOptimized(JPEG.toString());
    public static final CharSequence CONTENT_TYPE_IMAGE_PNG = HttpHeaders.createOptimized(PNG.toString());
    public static final CharSequence CONTENT_TYPE_IMAGE_ICO = HttpHeaders.createOptimized(ICO.toString());
}
