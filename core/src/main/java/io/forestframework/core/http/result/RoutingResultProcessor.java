package io.forestframework.core.http.result;

import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.routing.Routing;
import org.apiguardian.api.API;

/**
 * Usually, a processor returns returnValue again, so the next processor can process it.
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public interface RoutingResultProcessor {
    Object processResponse(WebContext context, Routing routing, Object returnValue);
}
