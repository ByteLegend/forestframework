package io.forestframework.core.http.websocket;

import com.google.inject.Injector;
import io.forestframework.core.http.ArgumentInjector;
import io.forestframework.core.http.RoutingMatchResults;
import io.forestframework.core.http.WebContext;
import org.apiguardian.api.API;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * For internal use only.
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public abstract class AbstractWebContext implements WebContext {
    private final RoutingMatchResults routingMatchResults;
    private final ArgumentInjector argumentInjector;
    private final ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();

    public AbstractWebContext(Injector injector, RoutingMatchResults routingMatchResults) {
        this.argumentInjector = new ArgumentInjector(injector);
        this.routingMatchResults = routingMatchResults;
    }

    public ArgumentInjector getArgumentInjector() {
        return argumentInjector;
    }

    public RoutingMatchResults getRoutingMatchResults() {
        return routingMatchResults;
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
