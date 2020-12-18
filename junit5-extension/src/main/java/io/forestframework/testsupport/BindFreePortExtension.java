package io.forestframework.testsupport;

import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.ApplicationContext;
import io.forestframework.testsupport.utils.FreePortFinder;

public class BindFreePortExtension implements Extension {
    @Override
    public void start(ApplicationContext applicationContext) {
        applicationContext.getConfigProvider().addConfig("forest.http.port", "" + FreePortFinder.findFreeLocalPort());
    }
}
