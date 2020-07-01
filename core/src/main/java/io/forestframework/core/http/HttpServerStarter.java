package io.forestframework.core.http;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultHttpServerStarter.class)
public interface HttpServerStarter {
    void start();
}
