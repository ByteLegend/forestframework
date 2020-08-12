package io.forestframework.core.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.forestframework.core.http.ChainedHttpRequestHandlers;
import io.forestframework.core.http.ChainedRequestHandler;
import io.forestframework.core.http.DefaultHttpRequestHandler;
import io.forestframework.core.http.HttpRequestHandler;
import io.forestframework.core.http.RoutingMatcher;
import io.forestframework.core.http.routing.DefaultRoutingManager;
//import io.forestframework.core.http.routing.FastRequestHandler;
//import io.forestframework.core.http.routing.Http404RequestHandler;
import io.forestframework.core.http.routing.RouterRequestHandler;
import io.forestframework.core.http.routing.RoutingManager;
//import io.forestframework.core.http.websocket.DefaultWebSocketRequestHandler;
//import io.forestframework.core.http.websocket.WebSocketRequestHandler;
import io.forestframework.core.http.websocket.DefaultWebSocketRequestHandler;
import io.forestframework.core.http.websocket.WebSocketRequestHandler;
import org.apiguardian.api.API;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@API(status = API.Status.INTERNAL, since = "0.1")
public class WebRequestHandlingModule extends AbstractModule {
    @Override
    protected void configure() {
        super.configure();
        bind(HttpRequestHandler.class).to(DefaultHttpRequestHandler.class);
        bind(RoutingManager.class).to(DefaultRoutingManager.class);
        bind(WebSocketRequestHandler.class).to(DefaultWebSocketRequestHandler.class);
//        bind(RoutingManager.class).to(DefaultRoutingManager.class);
    }

//    @Provides
//    @Singleton
//    @ChainedHttpRequestHandlers
//    public List<ChainedRequestHandler> httpRequestHandlers(
//            Http404RequestHandler http404RequestHandler,
//            FastRequestHandler fastRequestHandler,
//            RouterRequestHandler routerRequestHandler) {
//        return Collections.unmodifiableList(Arrays.asList(http404RequestHandler, fastRequestHandler, routerRequestHandler));
//    }
}
