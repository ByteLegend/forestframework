package io.forestframework.core.http.routing;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.MediaType;
import io.forestframework.core.http.DefaultHttpRequest;
import io.forestframework.core.http.DefaultPlainHttpRequestHandler;
import io.forestframework.core.http.HttpMethod;
import io.forestframework.core.http.HttpStatusCode;
import io.forestframework.core.http.bridge.DefaultBridgeRequestHandler;
import io.forestframework.core.http.websocket.DefaultWebSocketRequestHandler;
import io.vertx.core.http.HttpServerRequest;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.forestframework.core.http.OptimizedHeaders.HEADER_ACCEPT;
import static io.forestframework.core.http.OptimizedHeaders.HEADER_CONTENT_TYPE;

public class PlainHttpRoutingMatchResult implements RoutingMatchResult {
    private final Map<RoutingType, TreeSet<PlainHttpHandlerMatchResult>> plainHttpRoutings = new HashMap<>();
    private final Map<Routing, PlainHttpHandlerMatchResult> routingToResults = new HashMap<>();

    @Override
    public void select(HttpServerRequest request,
                       DefaultBridgeRequestHandler bridgeRequestHandler,
                       DefaultWebSocketRequestHandler webSocketRequestHandler,
                       DefaultPlainHttpRequestHandler plainHttpRequestHandler) {
        plainHttpRequestHandler.handle(new DefaultHttpRequest(request, this));
    }

    public void addResult(HttpServerRequest request, List<Routing> routings, Map<String, String> pathVariables) {
        routings.stream()
            .collect(Collectors.groupingBy(Routing::getType))
            .forEach((key, value) -> putIntoResult(request, key, value, pathVariables));
    }

    private void putIntoResult(HttpServerRequest request, RoutingType routingType, List<Routing> routings, Map<String, String> pathVariables) {
        TreeSet<PlainHttpHandlerMatchResult> routingMatchResults = getMatchResultsByType(routingType);
        for (Routing routing : routings) {
            PlainHttpHandlerMatchResult routingMatchResult = new PlainHttpHandlerMatchResult(routing, request, pathVariables);
            routingMatchResults.add(routingMatchResult);
            routingToResults.put(routing, routingMatchResult);
        }
    }

    @VisibleForTesting
    TreeSet<PlainHttpHandlerMatchResult> getMatchResultsByType(RoutingType routingType) {
        return plainHttpRoutings.computeIfAbsent(routingType, __ -> new TreeSet<>());
    }

    public List<Routing> getMatchingHandlersByType(RoutingType routingType) {
        return getMatchResultsByType(routingType)
            .stream()
            .filter(PlainHttpHandlerMatchResult::matches)
            .map(PlainHttpHandlerMatchResult::getRouting)
            .collect(Collectors.toList());
    }

    public PlainHttpHandlerMatchResult getMatchResultByRouting(Routing routing) {
        return routingToResults.get(routing);
    }

    /**
     * 200 If everything fine
     * <p>
     * 404 If no route matches the path
     * <p>
     * 405 If a route matches the path but don't match the HTTP Method
     * <p>
     * 406 If a route matches the path and the method but It can't provide a response with a content type matching Accept header
     * <p>
     * 415 If a route matches the path and the method but It can't accept the Content-type
     */
    public MainHandlerMatchResult getMainHandlerMatchResult() {
        return new MainHandlerMatchResult(getMatchResultsByType(RoutingType.HANDLER));
    }

    public Routing getMatchingErrorHandler(HttpStatusCode statusCode) {
        return getMatchResultsByType(RoutingType.ERROR_HANDLER)
            .stream()
            .map(PlainHttpHandlerMatchResult::getRouting)
            .filter(routing -> canHandle(routing, statusCode))
            .findFirst()
            .orElse(null);
    }

    private boolean canHandle(Routing routing, HttpStatusCode statusCode) {
        OnError errorHandler = AnnotationMagic.getOneAnnotationOnMethodOrNull(routing.getHandlerMethod(), OnError.class);
        if (errorHandler.start() == -1 || errorHandler.end() == -1) {
            return errorHandler.statusCode() == statusCode;
        } else {
            return errorHandler.start() <= statusCode.getCode() && statusCode.getCode() < errorHandler.end();
        }
    }

    public static class MainHandlerMatchResult {
        private HttpStatusCode statusCode = HttpStatusCode.NOT_FOUND;
        private final List<Routing> mainHandlers = new ArrayList<>();

        public MainHandlerMatchResult(Collection<PlainHttpHandlerMatchResult> matchResults) {
            for (PlainHttpHandlerMatchResult result : matchResults) {
                if (result.getCode() == HttpStatusCode.OK) {
                    statusCode = result.getCode();
                    mainHandlers.add(result.getRouting());
                } else if (statusCode != HttpStatusCode.OK) {
                    statusCode = result.getCode();
                }
            }
        }

        public HttpStatusCode getStatusCode() {
            return statusCode;
        }

        /**
         * Not only path, but http method, produces, consumes, everything matches.
         */
        public List<Routing> getExactlyMatchedHandlers() {
            return mainHandlers;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static class PlainHttpHandlerMatchResult implements Comparable<PlainHttpHandlerMatchResult> {
        private static final List<MediaType> ANY = Collections.singletonList(MediaType.ANY_TYPE);
        private static final Comparator<Routing> ROUTING_COMPARATOR =
            Comparator.comparing(Routing::getOrder)
                .thenComparing(routing -> routing.getHandlerMethod().toString());

        // 200, 405, 406, 415
        private final HttpStatusCode code;
        private final Routing routing;
        private final Map<String, String> pathParams;

        public PlainHttpHandlerMatchResult(Routing routing, HttpServerRequest request, Map<String, String> pathParams) {
            this.routing = routing;
            this.pathParams = pathParams;
            this.code = mediaTypeMatch(request, routing);
        }

        public HttpStatusCode getCode() {
            return code;
        }

        public Map<String, String> getPathParams() {
            return pathParams;
        }

        public Routing getRouting() {
            return routing;
        }

        public boolean matches() {
            return code == HttpStatusCode.OK;
        }

        private HttpStatusCode mediaTypeMatch(HttpServerRequest request, Routing routing) {
            if (httpMethodNotAllowed(routing, request.method())) {
                return HttpStatusCode.METHOD_NOT_ALLOWED;
            } else if (!producesMatch(request, HEADER_ACCEPT, routing.getProduces())) {
                return HttpStatusCode.NOT_ACCEPTABLE;
            } else if (!consumesMatch(request, HEADER_CONTENT_TYPE, routing.getConsumes())) {
                return HttpStatusCode.UNSUPPORTED_MEDIA_TYPE;
            } else {
                return HttpStatusCode.OK;
            }
        }

        private boolean producesMatch(HttpServerRequest request, CharSequence headerName, List<String> produces) {
            return mediaTypeMatch(getMediaTypes(request, headerName), getServerMediaTypes(produces));
        }

        private boolean consumesMatch(HttpServerRequest request, CharSequence headerName, List<String> consumes) {
            return mediaTypeMatch(getServerMediaTypes(consumes), getMediaTypes(request, headerName));
        }

        private boolean mediaTypeMatch(List<MediaType> accepts, List<MediaType> comparedMediaTypes) {
            return comparedMediaTypes.stream().anyMatch(
                comparedMediaType -> accepts.stream().anyMatch(accept -> compare(comparedMediaType, accept))
            );
        }

        private boolean compare(MediaType compared, MediaType accept) {
            return (compared.withoutParameters().is(accept.withoutParameters())
                || accept.withoutParameters().is(compared.withoutParameters()))
                && matchParameters(accept, compared);
        }

        private List<MediaType> getMediaTypes(HttpServerRequest request, CharSequence headerName) {
            String header = request.getHeader(headerName);

            return header == null
                ? ANY
                : Stream.of(StringUtils.split(header, ','))
                        .map(String::trim)
                        .map(MediaType::parse)
                        .collect(Collectors.toList());
        }

        private static List<MediaType> getServerMediaTypes(List<String> producesOrConsumes) {
            return producesOrConsumes.stream().map(MediaType::parse).collect(Collectors.toList());
        }

        private boolean matchParameters(MediaType accept, MediaType compared) {
            if (!accept.parameters().isEmpty() && !compared.parameters().isEmpty()) {
                for (String name : compared.parameters().keySet()) {
                    if (accept.parameters().containsKey(name)) {
                        return compared.parameters().get(name).containsAll(accept.parameters().get(name));
                    }
                }
            }
            return true;
        }

        private boolean httpMethodNotAllowed(Routing routing, io.vertx.core.http.HttpMethod method) {
            List<HttpMethod> routingMethods = routing.getMethods();
            HttpMethod requestMethod = HttpMethod.fromVertHttpMethod(method);
            for (HttpMethod m : routingMethods) {
                if (m == HttpMethod.ALL) {
                    return false;
                }
                if (m == requestMethod) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int compareTo(@NotNull PlainHttpHandlerMatchResult that) {
            return ROUTING_COMPARATOR.compare(this.routing, that.routing);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PlainHttpHandlerMatchResult that = (PlainHttpHandlerMatchResult) o;
            return routing.equals(that.routing);
        }

        @Override
        public int hashCode() {
            return Objects.hash(routing);
        }
    }
}
