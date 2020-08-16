package io.forestframework.core.http.routing;

import io.forestframework.utils.Pair;
import org.apiguardian.api.API;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultRoutingManager implements RoutingManager {
    private Map<RoutingType, List<Routing>> routings = new HashMap<>();

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

    /**
     * Make routings unmodifiable after all extensions finish their work.
     */
    public void finalizeRoutings() {
        routings = Collections.unmodifiableMap(
                Stream.of(RoutingType.values())
                        .map(routingType -> Pair.of(routingType, decorate(getRouting(routingType))))
                        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight))
        );
    }

    private List<Routing> decorate(List<Routing> routings) {
        return routings.stream().map(CachingRoutingDecorator::new).collect(Collectors.toList());
    }
}
