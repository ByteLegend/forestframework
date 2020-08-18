package io.forestframework.core.http.routing;

import io.vertx.core.http.HttpServerRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.forestframework.core.http.routing.RoutingMatchResult.WebSocketRoutingMatchResult;

abstract class RouteSegmentNode {
    private final String name;
    private final List<RouteSegmentNode> children = new ArrayList<>();
    private final List<Routing> plainHttpRoutings = new ArrayList<>();
    private final List<WebSocketRouting> webSocketRoutings = new ArrayList<>();
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

    public List<Routing> getPlainHttpRoutings() {
        return plainHttpRoutings;
    }

    public List<WebSocketRouting> getWebSocketRoutings() {
        return webSocketRoutings;
    }

    public String getName() {
        return name;
    }

    public RouteSegmentNode addRouting(Routing routing) {
        if (routing.getType() == RoutingType.WEB_SOCKET) {
            webSocketRoutings.add((WebSocketRouting) routing);
        } else {
            plainHttpRoutings.add(routing);
        }
        return this;
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
    public abstract boolean visit(AtomicReference<RoutingMatchResult> matchResult, HttpServerRequest request, String[] segments, int index, Map<String, String> pathVariables);

    protected boolean alreadySeenMatchingWebSocket(AtomicReference<RoutingMatchResult> matchResult) {
        return matchResult.get() instanceof WebSocketRoutingMatchResult;
    }

    protected void addResult(AtomicReference<RoutingMatchResult> matchResult, HttpServerRequest request, Map<String, String> pathVariables) {
        if (!webSocketRoutings.isEmpty()) {
            matchResult.set(new RoutingMatchResult.WebSocketRoutingMatchResult(webSocketRoutings.get(0).getPath(), pathVariables));
        } else {
            PlainHttpRoutingMatchResult plainHttpRoutingMatchResult = (PlainHttpRoutingMatchResult) matchResult.get();
            plainHttpRoutingMatchResult.addResult(request, plainHttpRoutings, pathVariables);
        }
    }
}


class StringConstantNode extends RouteSegmentNode {
    public StringConstantNode(String name) {
        super(name);
    }

    @Override
    public boolean visit(AtomicReference<RoutingMatchResult> matchResult, HttpServerRequest request, String[] segments, int index, Map<String, String> pathVariables) {
        if (index == segments.length - 1) {
            if (getName().equals(segments[index])) {
                addResult(matchResult, request, pathVariables);
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
    public boolean visit(AtomicReference<RoutingMatchResult> matchResult, HttpServerRequest request, String[] segments, int index, Map<String, String> pathVariables) {
        pathVariables.put(variableName, segments[index]);

        if (index == segments.length - 1) {
            addResult(matchResult, request, pathVariables);
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
    public boolean visit(AtomicReference<RoutingMatchResult> matchResult, HttpServerRequest request, String[] segments, int index, Map<String, String> pathVariables) {
        if (pattern.matcher(request.path()).matches()) {
            addResult(matchResult, request, pathVariables);
        }
        return false;
    }
}

class DoubleStarWildcardNode extends RouteSegmentNode {
    public DoubleStarWildcardNode() {
        super("**");
    }

    @Override
    public boolean visit(AtomicReference<RoutingMatchResult> matchResult, HttpServerRequest request, String[] segments, int index, Map<String, String> pathVariables) {
        if (!getPlainHttpRoutings().isEmpty()) {
            pathVariables.put("**", join(segments, index, segments.length));
            addResult(matchResult, request, pathVariables);
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
    public boolean visit(AtomicReference<RoutingMatchResult> matchResult, HttpServerRequest request, String[] segments, int index, Map<String, String> pathVariables) {
        Matcher matcher = pattern.matcher(segments[index]);
        if (matcher.find()) {
            pathVariables.put("*", matcher.group("wildcard"));
        }
        boolean matches = matcher.matches();

        if (index == segments.length - 1) {
            if (matches) {
                addResult(matchResult, request, pathVariables);
            }
            return false;
        } else {
            return matches;
        }
    }
}
