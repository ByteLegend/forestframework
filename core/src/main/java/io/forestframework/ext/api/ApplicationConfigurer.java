package io.forestframework.ext.api;

import com.google.inject.Injector;

public interface ApplicationConfigurer {
    void configure(Injector injector);
}
