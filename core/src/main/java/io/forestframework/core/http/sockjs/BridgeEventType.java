package io.forestframework.core.http.sockjs;

/**
 * Copied from Vertx web so users don't need to depend on it at compile time.
 */
public enum BridgeEventType {
    /**
     * This event will occur when a new SockJS socket is created.
     */
    SOCKET_CREATED,

    /**
     * This event will occur when a SockJS socket is closed.
     */
    SOCKET_CLOSED,

    /**
     * This event will occur when SockJS socket is on idle for longer period of time than configured.
     */
    SOCKET_IDLE,

    /**
     * This event will occur when the last ping timestamp is updated for the SockJS socket.
     */
    SOCKET_PING,

    /**
     * This event will occur when a message is attempted to be sent from the client to the server.
     */
    SEND,

    /**
     * This event will occur when a message is attempted to be published from the client to the server.
     */
    PUBLISH,

    /**
     * This event will occur when a message is attempted to be delivered from the server to the client.
     */
    RECEIVE,

    /**
     * This event will occur when a client attempts to register a handler.
     */
    REGISTER,

    /**
     * This event will occur when a client successfully registered. The raw message used for registration, notified with {@link io.vertx.ext.bridge.BridgeEventType#REGISTER} event
     */
    REGISTERED,

    /**
     * This event will occur when a client attempts to unregister a handler.
     */
    UNREGISTER,

    /**
     * Extra event type provided by Forest framework.
     */
    SOCKET_ERROR;


    static BridgeEventType fromVertxType(io.vertx.ext.bridge.BridgeEventType vertxType) {
        switch (vertxType) {
            case SOCKET_CREATED:
                return SOCKET_CREATED;
            case SOCKET_CLOSED:
                return SOCKET_CLOSED;
            case SOCKET_IDLE:
                return SOCKET_IDLE;
            case SOCKET_PING:
                return SOCKET_PING;
            case SEND:
                return SEND;
            case PUBLISH:
                return PUBLISH;
            case RECEIVE:
                return RECEIVE;
            case REGISTER:
                return REGISTER;
            case REGISTERED:
                return REGISTERED;
            case UNREGISTER:
                return UNREGISTER;
            default:
                throw new IllegalArgumentException("Unrecognize: " + vertxType);
        }
    }
}
