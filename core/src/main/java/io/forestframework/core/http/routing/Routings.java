package io.forestframework.core.http.routing;

import java.util.List;

public interface Routings {
    List<Routing> getRouting(RoutingType routingType);
}
