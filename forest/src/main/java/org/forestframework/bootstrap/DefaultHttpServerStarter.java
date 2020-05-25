package org.forestframework.bootstrap;

import org.forestframework.http.RoutingEngine;

import javax.inject.Inject;

public class DefaultHttpServerStarter implements HttpServerStarter {
    private final RoutingEngine routingEngine;

    @Inject
    public DefaultHttpServerStarter(RoutingEngine routingEngine) {
        this.routingEngine = routingEngine;
    }

    @Override
    public void start() {

    }
}
