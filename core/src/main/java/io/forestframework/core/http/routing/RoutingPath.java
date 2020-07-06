package io.forestframework.core.http.routing;

import com.google.common.collect.ImmutableMap;
import io.forestframework.core.http.HttpStatusCode;
import io.forestframework.core.http.OptimizedHeaders;
import io.forestframework.core.http.RerouteNextAwareRoutingContextDecorator;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Predicate;

/**
 * A {@link RoutingPath} represents a path by which a request passes. Each node on this path is represented by a {@link Routing} instance.
 * For example,
 * <ol>
 *     <li>Before-handlers are skipped when any previous pre-handler returns false or throws exceptions.</li>
 *     <li>Handlers are skipped when any previous pre-handler returns false or any pre-handler/handler throws exceptions.</li>
 *     <li>After-handlers are skipped only when previous after-handlers throw exceptions, but not when previous handlers return false or throw exceptions.</li>
 * </ol>
 *
 * Also see {@link RoutingType}.
 */
public class RoutingPath {
    private static final Map<RoutingType, Predicate<RoutingPath>> PREDICATES = ImmutableMap.of(
            RoutingType.PRE_HANDLER, RoutingPath::shouldSkipPreHandler,
            RoutingType.HANDLER, RoutingPath::shouldSkipHandler,
            RoutingType.POST_HANDLER, RoutingPath::shouldSkipPostHandler
    );
    private final RerouteNextAwareRoutingContextDecorator context;
    private final Queue<RoutingPathNode> nodes = new LinkedList<>();

    public RoutingPath(RerouteNextAwareRoutingContextDecorator context) {
        this.context = context;
    }

    public RoutingPathNode addNode(Routing routing) {
        RoutingPathNode ret = new RoutingPathNode(routing);
        ret.skipped = PREDICATES.get(routing.getType()).test(this);
        nodes.add(ret);
        return ret;
    }

    public void next() {
        // User invokes next()/reroute() already
        if (context.isNextInvoked() || context.isRerouteInvoked()) {
            return;
        }
        context.next();
    }

    private boolean shouldSkipPreHandler() {
        return nodes.stream().anyMatch(RoutingPathNode::isPreHandlerWhichReturnsFalseOrThrowsExceptions);
    }

    private boolean shouldSkipHandler() {
        return nodes.stream().anyMatch(node -> node.isPreHandlerWhichReturnsFalseOrThrowsExceptions() || node.isHandlerWhichThrowsExceptions());
    }

    private boolean shouldSkipPostHandler() {
        return nodes.stream().anyMatch(RoutingPathNode::isPostHandlerWhichThrowsExceptions);
    }

    public boolean noHandlerInvoked() {
        return nodes.stream().noneMatch(node -> node.routing.getType() == RoutingType.HANDLER);
    }

    public boolean hasFailures() {
        return nodes.stream().anyMatch(node -> node.failure != null);
    }

    public void respond500(boolean devMode) {
        Throwable failure = nodes.stream().filter(node -> node.failure != null).findFirst().get().failure;
        context.response().setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode());
        if (devMode) {
            context.response().putHeader(OptimizedHeaders.HEADER_CONTENT_TYPE, OptimizedHeaders.CONTENT_TYPE_TEXT_PLAIN);
            context.response().end(ExceptionUtils.getStackTrace(failure));
        }
    }

    public static class RoutingPathNode {
        private final Routing routing;
        private boolean skipped;
        private Object result;
        private Throwable failure;

        private RoutingPathNode(Routing routing) {
            this.routing = routing;
        }

        public boolean isSkipped() {
            return skipped;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public Throwable getFailure() {
            return failure;
        }

        public void setFailure(Throwable failure) {
            this.failure = failure;
        }

        private boolean isPreHandlerWhichReturnsFalseOrThrowsExceptions() {
            return routing.getType() == RoutingType.PRE_HANDLER &&
                    result == Boolean.FALSE &&
                    failure != null;
        }

        private boolean isHandlerWhichThrowsExceptions() {
            return routing.getType() == RoutingType.HANDLER && failure != null;
        }

        private boolean isPostHandlerWhichThrowsExceptions() {
            return routing.getType() == RoutingType.POST_HANDLER && failure != null;
        }
    }
}
