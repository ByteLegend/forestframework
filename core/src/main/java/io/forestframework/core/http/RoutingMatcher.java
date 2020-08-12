package io.forestframework.core.http;

import io.forestframework.core.http.routing.Routing;
import io.forestframework.core.http.routing.RoutingManager;
import io.forestframework.core.http.routing.RoutingType;
import io.forestframework.utils.Assert;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

abstract class RouteSegmentNode {
    private final String name;
    private final List<RouteSegmentNode> children = new ArrayList<>();
    private final List<Routing> routings = new ArrayList<>();
    private int index;

    public RouteSegmentNode(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public List<RouteSegmentNode> getChildren() {
        return children;
    }

    public List<Routing> getRoutings() {
        return routings;
    }

    public String getName() {
        return name;
    }

    public RouteSegmentNode addChild(RouteSegmentNode child) {
        Optional<RouteSegmentNode> existing = children.stream().filter(node -> node.getName().equals(child.name)).findFirst();
        if (existing.isPresent()) {
            return existing.get();
        } else {
            child.index = index + 1;
            children.add(child);
            return child;
        }
    }

    /**
     * Returns true if should continue visiting children, false otherwise.
     */
    public abstract boolean visit(RoutingMatchResults matchResult, HttpServerRequest request, String[] segments, int index, Map<String, String> pathVariables);
}

class StringConstantNode extends RouteSegmentNode {
    public StringConstantNode(String name) {
        super(name);
    }

    @Override
    public boolean visit(RoutingMatchResults matchResult, HttpServerRequest request, String[] segments, int index, Map<String, String> pathVariables) {
        if (index == segments.length - 1) {
            if (getName().equals(segments[index])) {
                matchResult.addResult(request, getRoutings(), pathVariables);
            }
            return false;
        } else {
            return getName().equals(segments[index]);
        }
    }
}

class PathVariableNode extends RouteSegmentNode {
    private final String variableName;

    public PathVariableNode(String segment) {
        super(segment);
        this.variableName = segment.substring(1);
    }

    @Override
    public boolean visit(RoutingMatchResults matchResult, HttpServerRequest request, String[] segments, int index, Map<String, String> pathVariables) {
        pathVariables.put(variableName, segments[index]);

        if (index == segments.length - 1) {
            matchResult.addResult(request, getRoutings(), pathVariables);
            return false;
        } else {
            return true;
        }
    }
}

class RegexNode extends RouteSegmentNode {
    private final Pattern pattern;

    public RegexNode(String regex) {
        super(regex);
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean visit(RoutingMatchResults matchResult, HttpServerRequest request, String[] segments, int index, Map<String, String> pathVariables) {
        if (pattern.matcher(request.path()).matches()) {
            matchResult.addResult(request, getRoutings(), pathVariables);
        }
        return false;
    }
}


class DoubleStarWildcardNode extends RouteSegmentNode {
    public DoubleStarWildcardNode() {
        super("**");
    }

    @Override
    public boolean visit(RoutingMatchResults matchResult, HttpServerRequest request, String[] segments, int index, Map<String, String> pathVariables) {
        if (!getRoutings().isEmpty()) {
            pathVariables.put("**", join(segments, index, segments.length));
            matchResult.addResult(request, getRoutings(), pathVariables);
        }
        // Take over the searching process
        // ** match 0,1, ..., all elements
        for (int i = index; i < segments.length; ++i) {
            int iCopy = i;
            Map<String, String> pathVariablesCopy = new HashMap<>(pathVariables);
            pathVariablesCopy.put("**", join(segments, index, i));
            getChildren().forEach(child -> RoutingMatcher.visit(matchResult, request, child, segments, iCopy, pathVariablesCopy));
        }

        return false;
    }

    private String join(String[] segment, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; ++i) {
            sb.append(segment[i]);
            if (i != end - 1) {
                sb.append('/');
            }
        }
        return sb.toString();
    }
}

class SingleStarWildcardNode extends RouteSegmentNode {
    private final Pattern pattern;

    public SingleStarWildcardNode(String name) {
        super(name);

        int lastStarIndex = StringUtils.lastIndexOf(name, '*');
        pattern = Pattern.compile(
                name.substring(0, lastStarIndex).replace("*", "(.*)") +
                        name.substring(lastStarIndex).replace("*", "(?<wildcard>.*)")
        );
    }

    @Override
    public boolean visit(RoutingMatchResults matchResult, HttpServerRequest request, String[] segments, int index, Map<String, String> pathVariables) {
        Matcher matcher = pattern.matcher(segments[index]);
        if (matcher.find()) {
            pathVariables.put("*", matcher.group("wildcard"));
        }
        boolean matches = matcher.matches();

        if (index == segments.length - 1) {
            if (matches) {
                matchResult.addResult(request, getRoutings(), pathVariables);
            }
            return false;
        } else {
            return matches;
        }
    }
}

@Singleton
public class RoutingMatcher {
    // Special handling for "/"
    private static final String[] ROOT_PATH = new String[]{""};
    RouteSegmentNode root = new StringConstantNode("");

    @Inject
    public RoutingMatcher(RoutingManager routingManager) {
        Stream.of(RoutingType.values()).forEach(type -> addRoutings(routingManager.getRouting(type)));
    }

    private void addRoutings(List<Routing> routings) {
        for (Routing routing : routings) {
            if (StringUtils.isNotBlank(routing.getRegexPath())) {
                root.addChild(new RegexNode(routing.getRegexPath())).getRoutings().add(routing);
            } else {
                addNonRegexPath(routing).getRoutings().add(routing);
            }
        }
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

    private String[] split(String path) {
        String[] segments = StringUtils.split(path, '/');
        return segments.length == 0 ? ROOT_PATH : segments;
    }

    public RoutingMatchResults match(HttpServerRequest request) {
        String[] pathSegments = split(request.path());
        RoutingMatchResults matchResult = new RoutingMatchResults();
        root.getChildren().forEach(child -> visit(matchResult, request, child, pathSegments, 0, new HashMap<>(pathSegments.length)));
        return matchResult;
    }

    public RoutingMatchResults match(ServerWebSocket socket) {
        String[] pathSegments = split(socket.path());
        RoutingMatchResults matchResult = new RoutingMatchResults();
        root.getChildren().forEach(child -> visit(matchResult, null, child, pathSegments, 0, new HashMap<>(pathSegments.length)));
        return matchResult;
    }

    static void visit(RoutingMatchResults result, HttpServerRequest request, RouteSegmentNode current, String[] path, int index, Map<String, String> pathVariables) {
        if (current.visit(result, request, path, index, pathVariables)) {
            current.getChildren().forEach(child -> visit(result, request, child, path, index + 1, new HashMap<>(pathVariables)));
        }
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

