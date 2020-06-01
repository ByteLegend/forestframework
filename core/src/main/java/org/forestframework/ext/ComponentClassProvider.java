package org.forestframework.ext;

import javax.inject.Provider;
import java.util.List;

public interface ComponentClassProvider extends Provider<List<Class<?>>> {
    @Override
    List<Class<?>> get();
}
