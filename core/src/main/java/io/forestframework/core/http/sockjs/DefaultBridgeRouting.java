package io.forestframework.core.http.sockjs;

import io.forestframework.core.http.DefaultRouting;
import io.forestframework.core.http.HttpMethod;
import io.forestframework.core.http.routing.BridgeRouting;
import io.forestframework.core.http.routing.RoutingType;
import org.apiguardian.api.API;

import java.lang.reflect.Method;
import java.util.List;

/**
 * For internal use only.
 */
@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultBridgeRouting extends DefaultRouting implements BridgeRouting {
    private final List<BridgeEventType> eventTypes;

    public DefaultBridgeRouting(boolean blocking, RoutingType type, String path, String regexPath, List<HttpMethod> methods, Method handlerMethod, List<BridgeEventType> eventTypes) {
        super(blocking, type, path, regexPath, methods, handlerMethod);
        this.eventTypes = eventTypes;
    }

    @Override
    public List<BridgeEventType> getEventTypes() {
        return eventTypes;
    }
}
