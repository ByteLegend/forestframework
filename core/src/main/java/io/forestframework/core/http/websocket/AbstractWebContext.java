package io.forestframework.core.http.websocket;

import com.google.inject.Injector;
import io.forestframework.core.http.ArgumentInjector;
import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.routing.Routing;
import org.apiguardian.api.API;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * For internal use only.
 */
@API(status = API.Status.INTERNAL, since = "0.1")
public abstract class AbstractWebContext implements WebContext {
    private final ArgumentInjector argumentInjector;
    private final ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();
    private Routing routing;

    public AbstractWebContext(Injector injector) {
        this.argumentInjector = new ArgumentInjector(injector);
    }

    public ArgumentInjector getArgumentInjector() {
        return argumentInjector;
    }

    public ConcurrentHashMap<String, Object> getData() {
        return data;
    }

    public Routing getRouting() {
        return routing;
    }

    public void setRouting(Routing routing) {
        this.routing = routing;
    }

    @Override
    public WebContext put(String key, Object obj) {
        data.put(key, obj);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T remove(String key) {
        return (T) data.remove(key);
    }

    @Override
    public Map<String, Object> data() {
        return data;
    }
}
