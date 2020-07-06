package io.forestframework.core.http.routing;

import org.apiguardian.api.API;

import java.util.List;

@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public interface Routings {
    List<Routing> getRouting(RoutingType routingType);
}
