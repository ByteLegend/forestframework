package io.forestframework.core.http.routing;

import io.forestframework.core.http.sockjs.BridgeEventType;
import org.apiguardian.api.API;

import java.util.List;

@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public interface BridgeRouting extends Routing {
    List<BridgeEventType> getEventTypes();
}

