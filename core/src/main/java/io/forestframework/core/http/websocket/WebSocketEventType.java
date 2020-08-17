package io.forestframework.core.http.websocket;

import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public enum WebSocketEventType {
    OPEN,
    CLOSE,
    MESSAGE,
    ERROR
}
