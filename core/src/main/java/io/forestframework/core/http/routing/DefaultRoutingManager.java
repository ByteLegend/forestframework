package io.forestframework.core.http.routing;

import io.forestframework.core.http.bridge.BridgeEventType;
import io.forestframework.core.http.websocket.WebSocketEventType;
import io.forestframework.utils.Pair;
import org.apiguardian.api.API;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultRoutingManager implements RoutingManager {
    private Map<RoutingType, List<Routing>> allRoutings = new HashMap<>();
    private Map<String, Map<BridgeEventType, BridgeRouting>> bridgeRoutings;
    private Map<String, Map<WebSocketEventType, WebSocketRouting>> webSocketRoutings;

    @Override
    public List<Routing> getRouting(RoutingType routingType) {
        // Collections$UnmodifiableMap doesn't support computeIfAbsent
        List<Routing> ret = allRoutings.get(routingType);
        if (ret == null) {
            ret = new ArrayList<>();
            allRoutings.put(routingType, ret);
        }
        return ret;
    }

    /**
     * Make routings unmodifiable after all extensions finish their work.
     */
    public void finalizeRoutings() {
        allRoutings = Collections.unmodifiableMap(
                Stream.of(RoutingType.values())
                        .map(routingType -> Pair.of(routingType, decorate(getRouting(routingType))))
                        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight))
        );
        bridgeRoutings = reorganizeRoutings(getRouting(RoutingType.BRIDGE), BridgeRouting::getBridgeEventTypes);
        webSocketRoutings = reorganizeRoutings(getRouting(RoutingType.WEB_SOCKET), WebSocketRouting::getWebSocketEventTypes);
    }

    public Map<String, Map<BridgeEventType, BridgeRouting>> getBridgeRoutings() {
        return bridgeRoutings;
    }

    public Map<String, Map<WebSocketEventType, WebSocketRouting>> getWebSocketRoutings() {
        return webSocketRoutings;
    }

    @SuppressWarnings("unchecked")
    private <TYPE, ROUTING extends Routing> Map<String, Map<TYPE, ROUTING>> reorganizeRoutings(List<Routing> routings, Function<ROUTING, List<TYPE>> fn) {
        // A single path must has only one handler per event type
        Map<String, List<ROUTING>> pathToHandlers = routings
                .stream()
                .map(r -> (ROUTING) r)
                .collect(Collectors.groupingBy(Routing::getPath));

        Map<String, Map<TYPE, ROUTING>> ret = new HashMap<>();
        pathToHandlers.forEach((path, routingsOfSamePath) -> ret.put(path, validateAndRemapRoutings(path, routingsOfSamePath, fn)));
        return Collections.unmodifiableMap(ret);
    }


    private <TYPE, ROUTING extends Routing> Map<TYPE, ROUTING> validateAndRemapRoutings(String path, List<ROUTING> routings, Function<ROUTING, List<TYPE>> fn) {
        Map<TYPE, ROUTING> ret = new HashMap<>();
        for (ROUTING routing : routings) {
            for (TYPE type : fn.apply(routing)) {
                ROUTING oldRouting = ret.put(type, routing);
                if (oldRouting != null && !routing.getHandlerMethod().equals(oldRouting.getHandlerMethod())) {
                    // For a specific path, there should not be more than one handler mapped to the same event.
                    throw new IllegalArgumentException("Found more than one Bridge handler mapped to " + path + " on event " + type
                            + ":\n1. " + oldRouting.getHandlerMethod() + "\n2. " + routing.getHandlerMethod());
                }
            }
        }
        return Collections.unmodifiableMap(ret);
    }

    private List<Routing> decorate(List<Routing> routings) {
        return routings.stream().map(CachingRoutingDecorator::new).collect(Collectors.toList());
    }
}

