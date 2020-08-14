package io.forestframework.core.http;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.MediaType;
import io.forestframework.core.http.routing.OnError;
import io.forestframework.core.http.routing.Routing;
import io.forestframework.core.http.routing.RoutingType;
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
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.forestframework.core.http.OptimizedHeaders.HEADER_ACCEPT;
import static io.forestframework.core.http.OptimizedHeaders.HEADER_CONTENT_TYPE;

public class RoutingMatchResults {
    private final Map<RoutingType, TreeSet<RoutingMatchResult>> typeToResults = new HashMap<>();
    private final Map<Routing, RoutingMatchResult> routingToResults = new HashMap<>();

    public RoutingMatchResult getRoutingMatchResult(Routing routing) {
        return routingToResults.get(routing);
    }

    @VisibleForTesting
    TreeSet<RoutingMatchResult> getMatchedResults(RoutingType routingType) {
        return typeToResults.computeIfAbsent(routingType, __ -> new TreeSet<>());
    }

    public void addResult(HttpServerRequest request, List<Routing> routings, Map<String, String> pathVariables) {
        routings.stream()
                .collect(Collectors.groupingBy(Routing::getType))
                .forEach((key, value) -> putIntoResult(request, key, value, pathVariables));
    }

    public List<Routing> getMatchedRoutings(RoutingType routingType) {
        return getMatchedResults(routingType)
                .stream()
                .filter(RoutingMatchResult::matches)
                .map(RoutingMatchResult::getRouting)
                .collect(Collectors.toList());
    }

    /**
     * 200 If everything fine
     *
     * 404 If no route matches the path
     *
     * 405 If a route matches the path but don’t match the HTTP Method
     *
     * 406 If a route matches the path and the method but It can’t provide a response with a content type matching Accept header
     *
     * 415 If a route matches the path and the method but It can’t accept the Content-type
     */
    public HandlerMatchResult getHandlerMatchResult() {
        return new HandlerMatchResult(getMatchedResults(RoutingType.HANDLER));
    }

    public Routing getMatchedErrorHandler(HttpStatusCode statusCode) {
        return getMatchedResults(RoutingType.ERROR_HANDLER)
                .stream()
                .map(RoutingMatchResult::getRouting)
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

    public static class HandlerMatchResult {
        private HttpStatusCode statusCode = HttpStatusCode.NOT_FOUND;
        private final List<Routing> handlers = new ArrayList<>();

        public HandlerMatchResult(Collection<RoutingMatchResult> matchResults) {
            for (RoutingMatchResult result : matchResults) {
                if (result.code == HttpStatusCode.OK) {
                    statusCode = result.code;
                    handlers.add(result.routing);
                } else if (statusCode != HttpStatusCode.OK) {
                    statusCode = result.code;
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
            return handlers;
        }
    }


    private void putIntoResult(HttpServerRequest request, RoutingType routingType, List<Routing> routings, Map<String, String> pathVariables) {
        TreeSet<RoutingMatchResult> routingMatchResults = getMatchedResults(routingType);
        for (Routing routing : routings) {
            RoutingMatchResult routingMatchResult = new RoutingMatchResult(routing, request, pathVariables);
            routingToResults.put(routing, routingMatchResult);
            routingMatchResults.add(routingMatchResult);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static class RoutingMatchResult implements Comparable<RoutingMatchResult> {
        private static final List<MediaType> ANY = Collections.singletonList(MediaType.ANY_TYPE);
        private static final Comparator<Routing> ROUTING_COMPARATOR =
                Comparator.comparing(Routing::getOrder)
                        .thenComparing(Routing::getType)
                        .thenComparing(Routing::getPath)
                        .thenComparing(Routing::getRegexPath)
                        .thenComparing(routing -> routing.getMethods().toString())
                        .thenComparing(routing -> routing.getConsumes().toString())
                        .thenComparing(routing -> routing.getProduces().toString());

        // 200, 405, 406, 415
        HttpStatusCode code;
        Routing routing;
        Map<String, String> pathParams;

        public RoutingMatchResult(Routing routing, HttpServerRequest request, Map<String, String> pathParams) {
            this.routing = routing;
            this.pathParams = pathParams;
            // WebSocket
            this.code = request == null ? HttpStatusCode.OK : mediaTypeMatch(request, routing);
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
            } else if (!mediaTypeMatch(request, HEADER_ACCEPT, routing.getProduces())) {
                return HttpStatusCode.NOT_ACCEPTABLE;
            } else if (!mediaTypeMatch(request, HEADER_CONTENT_TYPE, routing.getConsumes())) {
                return HttpStatusCode.UNSUPPORTED_MEDIA_TYPE;
            } else {
                return HttpStatusCode.OK;
            }
        }

        private boolean mediaTypeMatch(HttpServerRequest request, CharSequence headerName, List<String> producesOrConsumes) {
            String header = request.getHeader(headerName);

            List<MediaType> headers = header == null ? ANY :
                    Stream.of(StringUtils.split(header, ',')).map(String::trim).map(MediaType::parse).collect(Collectors.toList());
            List<MediaType> serverMediaTypes = producesOrConsumes.stream().map(MediaType::parse).collect(Collectors.toList());

            return headers.stream().anyMatch(headerMediaType ->
                    serverMediaTypes.stream().anyMatch(headerMediaType::is)
            );
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
        public int compareTo(@NotNull RoutingMatchResult that) {
            return ROUTING_COMPARATOR.compare(this.routing, that.routing);
        }
    }
}
