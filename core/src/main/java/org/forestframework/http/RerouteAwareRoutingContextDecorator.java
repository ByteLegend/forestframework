package org.forestframework.http;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class RerouteAwareRoutingContextDecorator extends AbstractRoutingContextDecorator {
    private boolean rerouteInvoked;

    public RerouteAwareRoutingContextDecorator(RoutingContext delegate) {
        super(delegate);
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
