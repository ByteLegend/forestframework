package io.forestframework.core.http;

import io.forestframework.core.http.routing.RoutingType;
import io.forestframework.core.http.routing.SockJSRouting;
import io.forestframework.core.http.sockjs.SockJS;
import io.forestframework.core.http.sockjs.SockJSEventType;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//public class DefaultSockJSRouting extends DefaultRouting implements SockJSRouting {
//    private final List<SockJSEventType> eventTypes;
//
//    public DefaultSockJSRouting(SockJS sockJS, String path, Method handlerMethod) {
//        super(RoutingType.SOCK_JS, path, "", Collections.singletonList(HttpMethod.GET), handlerMethod);
//        this.eventTypes = Arrays.asList(sockJS.eventTypes());
//    }
//
//    @Override
//    public List<SockJSEventType> getEventTypes() {
//        return eventTypes;
//    }
//}
