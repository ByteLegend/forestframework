package io.forestframework.bootstrap;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultHttpServerStarter.class)
public interface HttpServerStarter {
    void start();
}
