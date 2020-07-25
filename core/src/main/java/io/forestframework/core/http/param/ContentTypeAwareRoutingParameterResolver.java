package io.forestframework.core.http.param;

public interface ContentTypeAwareRoutingParameterResolver<T> extends RoutingParameterResolver<T> {
    /**
     * The content-type this {@link RoutingParameterResolver} can handle.
     *
     * See RFC <a href="https://tools.ietf.org/html/rfc2045">2045</a> and <a href="https://tools.ietf.org/html/rfc2046">2046</a>.
     *
     * @return the supported content type
     */
    String contentType();
}
