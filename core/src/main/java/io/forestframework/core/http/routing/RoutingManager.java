package io.forestframework.core.http.routing;

import io.forestframework.core.injector.DefaultImplementedBy;
import org.apiguardian.api.API;

import java.util.List;

@DefaultImplementedBy(DefaultRoutingManager.class)
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public interface RoutingManager {
    List<Routing> getRouting(RoutingType routingType);
}
