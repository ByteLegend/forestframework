package org.forestframework.bootstrap;

import com.google.inject.Injector;

public interface InjectorCreator {
    Injector createInjector(Class<?> applicationClass);
}
