package io.forestframework.core.http.routing;

import io.forestframework.core.http.HttpException;
import io.forestframework.core.http.HttpMethod;
import io.forestframework.core.http.bridge.BridgeEventType;
import io.forestframework.core.http.routing.RoutingMatchResult.BridgeRoutingMatchResult;
import io.forestframework.core.http.websocket.WebSocketEventType;
import io.forestframework.utils.Assert;
import io.vertx.core.http.HttpServerRequest;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static io.forestframework.core.http.routing.RoutingMatchResult.WebSocketRoutingMatchResult;


/**
 * For internal use only.
 */
@API(status = API.Status.INTERNAL, since = "0.1")
@Singleton
public class RoutingMatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutingMatcher.class);
    // Special handling for "/"
    private static final String[] ROOT_PATH = new String[]{""};
    private final RouteSegmentNode root = new StringConstantNode("");
    private final Map<String, Map<BridgeEventType, BridgeRouting>> bridgeRoutings;
    private final Map<String, Map<WebSocketEventType, WebSocketRouting>> webSocketRoutings;

    @Inject
    public RoutingMatcher(RoutingManager routingManager) {
        Stream.of(RoutingType.values())
              .filter(type -> type != RoutingType.BRIDGE)
              .forEach(type -> addRoutings(routingManager.getRouting(type)));
        bridgeRoutings = ((DefaultRoutingManager) routingManager).getBridgeRoutings();
        webSocketRoutings = ((DefaultRoutingManager) routingManager).getWebSocketRoutings();
    }

    public RoutingMatchResult match(HttpServerRequest request) {
        try {
            RoutingMatchResult bridgeResult = matchBridge(request);
            if (bridgeResult != null) {
                return bridgeResult;
            }

            String[] pathSegments = split(request.path());
            AtomicReference<RoutingMatchResult> result = new AtomicReference<>(new PlainHttpRoutingMatchResult());
            root.getChildren().forEach(child -> visit(result, request, child, pathSegments, 0, new HashMap<>(pathSegments.length)));

            if (alreadySeenMatchedWebSocket(result)) {
                WebSocketRoutingMatchResult webSocketResult = (WebSocketRoutingMatchResult) result.get();
                webSocketResult.setRoutings(webSocketRoutings.get(webSocketResult.getPath()));
                return webSocketResult;
            } else {
                return result.get();
            }
        } catch (HttpException e) {
            LOGGER.error("", e);
            return new RoutingMatchResult.ErrorRoutingMatchResult(e);
        }
    }

    private BridgeRoutingMatchResult matchBridge(HttpServerRequest request) {
        String path = request.path();
        for (Map.Entry<String, Map<BridgeEventType, BridgeRouting>> entry : bridgeRoutings.entrySet()) {
            String routingPath = entry.getKey();
            if (path.equals(routingPath) || (path.startsWith(routingPath) && path.charAt(routingPath.length()) == '/')) {
                return new BridgeRoutingMatchResult(entry.getValue());
            }
        }
        return null;
    }

    static void visit(AtomicReference<RoutingMatchResult> result, HttpServerRequest request, RouteSegmentNode current, String[] path, int index, Map<String, String> pathVariables) {
        if (!alreadySeenMatchedWebSocket(result) && current.visit(result, request, path, index, pathVariables)) {
            current.getChildren().forEach(child -> visit(result, request, child, path, index + 1, new HashMap<>(pathVariables)));
        }
    }

    private static boolean alreadySeenMatchedWebSocket(AtomicReference<RoutingMatchResult> result) {
        return result.get() instanceof WebSocketRoutingMatchResult;
    }

    private void addRoutings(List<Routing> routings) {
        for (Routing routing : routings) {
            if (StringUtils.isNotBlank(routing.getRegexPath())) {
                root.addChild(new RegexNode(routing.getRegexPath())).addRouting(routing);
            } else {
                addNonRegexPath(routing).addRouting(routing);
            }
        }
    }

    private String[] split(String path) {
        String[] segments = StringUtils.split(path, '/');
        return segments.length == 0 ? ROOT_PATH : segments;
    }

    /**
     * Returns the leaf node
     */
    private RouteSegmentNode addNonRegexPath(Routing routing) {
        RouteSegmentNode current = root;

        for (String segment : split(routing.getPath())) {
            if (segment.startsWith(":")) {
                current = current.addChild(new PathVariableNode(segment));
            } else if (segment.contains("**")) {
                Assert.isTrue("**".equals(segment), "Only ** is supported in segment: " + segment);
                current = current.addChild(new DoubleStarWildcardNode());
            } else if (segment.contains("*")) {
                current = current.addChild(new SingleStarWildcardNode(segment));
            } else {
                current = current.addChild(new StringConstantNode(segment));
            }
        }

        return current;
    }
}

class CacheKey {
    private final String accept;
    private final String contentType;
    private final HttpMethod httpMethod;
    private final String path;

    public CacheKey(String accept, String contentType, HttpMethod httpMethod, String path) {
        this.accept = accept;
        this.contentType = contentType;
        this.httpMethod = httpMethod;
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CacheKey cacheKey = (CacheKey) o;
        return Objects.equals(accept, cacheKey.accept) &&
            Objects.equals(contentType, cacheKey.contentType) &&
            httpMethod == cacheKey.httpMethod &&
            Objects.equals(path, cacheKey.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accept, contentType, httpMethod, path);
    }
}

