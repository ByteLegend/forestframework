package org.forestframework.bootstrap;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultHttpServerStarter.class)
public interface HttpServerStarter {
    void start();
}
