package org.forestframework.http;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class RerouteNextAwareRoutingContextDecorator extends AbstractRoutingContextDecorator {
    private boolean rerouteInvoked;
    private boolean nextInvoked;

    public RerouteNextAwareRoutingContextDecorator(RoutingContext delegate) {
        super(delegate);
    }

    public void nextIfNotInvoked() {
        if (!nextInvoked) {
            next();
            nextInvoked = true;
        } else {
            nextInvoked = false;
        }
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
}
