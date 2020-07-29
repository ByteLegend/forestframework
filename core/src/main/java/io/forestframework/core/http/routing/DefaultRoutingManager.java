package io.forestframework.core.http.routing;

import io.forestframework.utils.Pair;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class DefaultRoutingManager implements RoutingManager {
    private Map<RoutingType, List<Routing>> routings = new HashMap<>();
    private Map<RoutingType, List<String>> routingPrefixes;

    @Override
    public List<Routing> getRouting(RoutingType routingType) {
        // Collections$UnmodifiableMap doesn't support computeIfAbsent
        List<Routing> ret = routings.get(routingType);
        if (ret == null) {
            ret = new ArrayList<>();
            routings.put(routingType, ret);
        }
        return ret;
    }

    public List<String> getRoutingPrefixes(RoutingType routingType) {
        return routingPrefixes.get(routingType);
    }

    /**
     * Make routings unmodifiable after all extensions finish their work.
     */
    public void finalizeRoutings() {
        routings = Collections.unmodifiableMap(
                Stream.of(RoutingType.values())
                        .map(routingType -> Pair.of(routingType, decorate(getRouting(routingType))))
                        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight))
        );
        routingPrefixes = Collections.unmodifiableMap(
                routings.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> createHandlerPathPrefixes(entry.getValue())))
        );
    }

    /**
     * The prefixes of all handlers. We can return 404 safely for the paths which doesn't match the prefixes.
     *
     * For example, for handlers with paths "/*", "/user/:id", "/order/:id", "/static/*", the prefixes would be
     * "/", "/user/", "/order/", "/static/"
     */
    private List<String> createHandlerPathPrefixes(List<Routing> routingList) {
        return Collections.unmodifiableList(
                routingList
                        .stream()
                        .map(this::extractPrefix)
                        .distinct()
                        .collect(Collectors.toList())
        );
    }

    private String extractPrefix(Routing routing) {
        if (routing.getPath().isEmpty()) {
            return "/";
        }
        StringBuilder sb = new StringBuilder();
        for (char ch : routing.getPath().toCharArray()) {
            if (ch == ':' || ch == '*') {
                break;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    private List<Routing> decorate(List<Routing> routings) {
        return routings.stream().map(this::decorate).collect(Collectors.toList());
    }

    private Routing decorate(Routing routing) {
        if (routing instanceof SockJSRouting) {
            return routing;
        } else {
            return new CachingRoutingDecorator(routing);
        }
    }
//
//    private Map<RoutingType, List<Routing>> createFinalizedRoutings() {
//        Map<RoutingType, List<Routing>> ret = routings.entrySet().stream()
//                .filter(entry -> entry.getKey() != RoutingType.HANDLER)
//                .collect(Collectors.toMap(Map.Entry::getKey, entry -> createSlowRoutings(entry.getValue())));
//
//        ret.put(RoutingType.HANDLER,
//                getRouting(RoutingType.HANDLER)
//                        .stream()
//                        .map(this::finalizeHandler)
//                        .collect(Collectors.toList()));
//        return ret;
//    }
//
//    private Routing finalizeHandler(Routing routing) {
//        if (!routing.getRegexPath().isEmpty()) {
//            return new CachingRoutingDecorator(routing);
//        } else if (hasRoutingContextParam(routing)) {
//            return new CachingRoutingDecorator(routing);
//        } else if (anyPrefixMatching(routing)) {
//            return new CachingRoutingDecorator(routing);
//        } else {
//            return new CachingRoutingDecorator(routing, true);
//        }
//    }
//
//    /**
//     * Is there any interceptor path prefix match this routing? If yes, we need to treat it as slow routing.
//     */
//    private boolean anyPrefixMatching(Routing routing) {
//        return false;
//    }
//
//    private boolean hasRoutingContextParam(Routing routing) {
//        return Arrays.asList(routing.getHandlerMethod().getParameterTypes()).contains(RoutingContext.class);
//    }
//
//    private List<Routing> createSlowRoutings(List<Routing> routings) {
//        return routings.stream().map(CachingRoutingDecorator::new).collect(Collectors.toList());
//    }
}
