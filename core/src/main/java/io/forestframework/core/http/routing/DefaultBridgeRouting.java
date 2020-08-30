package io.forestframework.core.http.routing;

import io.forestframework.core.http.bridge.BridgeEventType;
import org.apiguardian.api.API;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * For internal use only.
 */
@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultBridgeRouting extends DefaultRouting implements BridgeRouting {
    private final List<BridgeEventType> eventTypes;

    public DefaultBridgeRouting(boolean blocking, RoutingType type, String path, String regexPath, Method handlerMethod, List<BridgeEventType> eventTypes) {
        super(blocking, type, path, regexPath, emptyList(), handlerMethod, -1, emptyList(), emptyList());
        this.eventTypes = eventTypes;
    }

    @Override
    public List<BridgeEventType> getBridgeEventTypes() {
        return eventTypes;
    }
}
