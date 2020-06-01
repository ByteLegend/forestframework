package org.forestframework.ext.api;

import org.forestframework.http.Routing;

import java.util.List;

public interface RoutingConfigurer {
    void configure(List<Routing> routings);
}
