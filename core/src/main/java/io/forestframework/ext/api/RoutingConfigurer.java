package io.forestframework.ext.api;

import io.forestframework.http.Routing;

import java.util.List;

public interface RoutingConfigurer {
    void configure(List<Routing> routings);
}
