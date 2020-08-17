package io.forestframework.core.modules;

import com.google.inject.AbstractModule;
import io.forestframework.core.http.DefaultHttpRequestDispatcher;
import io.forestframework.core.http.HttpRequestHandler;
import io.forestframework.core.http.routing.DefaultRoutingManager;
import io.forestframework.core.http.routing.RoutingManager;
import org.apiguardian.api.API;

@API(status = API.Status.INTERNAL, since = "0.1")
public class WebRequestHandlingModule extends AbstractModule {
    @Override
    protected void configure() {
        super.configure();
        bind(HttpRequestHandler.class).to(DefaultHttpRequestDispatcher.class);
        bind(RoutingManager.class).to(DefaultRoutingManager.class);
    }
}
