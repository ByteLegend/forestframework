package io.forestframework.core.http.routing;

import io.forestframework.core.http.sockjs.SockJSEventType;
import org.apiguardian.api.API;

import java.util.List;

@API(status = API.Status.INTERNAL, since = "0.1")
public interface SockJSRouting extends Routing {
    List<SockJSEventType> getEventTypes();
}

