package io.forestframework.core.http.bridge;

import com.google.inject.Injector;
import io.forestframework.core.http.HttpRequest;
import io.forestframework.core.http.HttpResponse;
import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.websocket.AbstractWebContext;

import java.util.Map;

public class BridgeContext extends AbstractWebContext {
    public BridgeContext(Injector injector) {
        super(injector);
    }

    @Override
    public HttpRequest request() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpResponse response() {
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

    @Override
    public Map<String, String> pathParams() {
        throw new UnsupportedOperationException();
    }
}
