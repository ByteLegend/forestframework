package io.forestframework.core.http.sockjs;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

/**
 * Copied from Vert.x web so that users don't need it as compile dependency.
 *
 * Represents an event that occurs on the event bus bridge.
 * <p>
 * Please consult the documentation for a full explanation.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface BridgeEvent extends BaseBridgeEvent {

    /**
     * Set the raw JSON message for the event.
     *
     * @param message the raw message
     * @return this reference, so it can be used fluently
     */
    BridgeEvent setRawMessage(JsonObject message);

    /**
     * Get the SockJSSocket instance corresponding to the event
     *
     * @return  the SockJSSocket instance
     */
    SockJSSocket socket();
}
