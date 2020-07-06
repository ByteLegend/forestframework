package io.forestframework.core.http;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

/**
 * Records {@link RoutingContext#next()} and {@link RoutingContext#reroute(String)} invocations in user handlers to
 * make our routing engine work correctly.
 */
public class RerouteNextAwareRoutingContextDecorator extends AbstractRoutingContextDecorator {
    private boolean rerouteInvoked = false;
    private boolean nextInvoked = false;

    public RerouteNextAwareRoutingContextDecorator(RoutingContext delegate) {
        super(delegate);
    }

    @Override
    public void next() {
        nextInvoked = true;
        super.next();
    }

    @Override
    public void reroute(String path) {
        rerouteInvoked = true;
        super.reroute(path);
    }

    @Override
    public void reroute(HttpMethod method, String path) {
        rerouteInvoked = true;
        super.reroute(method, path);
    }

    public boolean isRerouteInvoked() {
        return rerouteInvoked;
    }

    public boolean isNextInvoked() {
        return nextInvoked;
    }

    public void reset() {
        rerouteInvoked = false;
        nextInvoked = false;
    }
}
