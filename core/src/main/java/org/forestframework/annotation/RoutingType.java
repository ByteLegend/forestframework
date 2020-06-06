package org.forestframework.annotation;

public enum RoutingType {
    /**
     * A route before the handler, similar to interceptor.
     * A pre handler route should return boolean or Future[Boolean] which indicates if the routing process should continue.
     * When pre handler returns false, all following handler/after success/after failure handlers are ignored, but
     * after completion handler will still be invoked.
     */
    PRE_HANDLER,
    HANDLER,
    AFTER_HANDLER_SUCCESS,
    AFTER_HANDLER_FAILURE,
    AFTER_HANDLER_COMPLETION
}
