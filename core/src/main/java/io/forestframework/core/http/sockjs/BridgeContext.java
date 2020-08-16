package io.forestframework.core.http.sockjs;

import com.google.inject.Injector;
import io.forestframework.core.http.RoutingMatchResults;
import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.websocket.AbstractWebContext;

import java.util.Map;

public class BridgeContext extends AbstractWebContext {
    public BridgeContext(Injector injector) {
        super(injector, null);
    }

    @Override
    public RoutingMatchResults getRoutingMatchResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebContext put(String key, Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T get(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T remove(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> data() {
        throw new UnsupportedOperationException();
    }
}
