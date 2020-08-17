package io.forestframework.core.http.bridge;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

/**
 * * Copied from Vert.x web so that users don't need it as compile dependency.
 *
 * Represents an event that occurs on the event bus bridge.
 * <p>
 * Please consult the documentation for a full explanation.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface BaseBridgeEvent extends Promise<Boolean> {

    /**
     * @return the type of the event
     */
    BridgeEventType type();

    /**
     * Get the raw JSON message for the event. This will be null for SOCKET_CREATED or SOCKET_CLOSED events as there is
     * no message involved. If the returned message is modified, {@link #setRawMessage} should be called with the
     * new message.
     *
     * @return the raw JSON message for the event
     */
    JsonObject getRawMessage();

    /**
     * Get the raw JSON message for the event. This will be null for SOCKET_CREATED or SOCKET_CLOSED events as there is
     * no message involved.
     *
     * @param message the raw message
     * @return this reference, so it can be used fluently
     */
    BaseBridgeEvent setRawMessage(JsonObject message);
}
