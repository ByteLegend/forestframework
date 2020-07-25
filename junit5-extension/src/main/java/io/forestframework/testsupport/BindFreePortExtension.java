package io.forestframework.testsupport;

import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;
import io.forestframework.testsupport.utils.FreePortFinder;

public class BindFreePortExtension implements Extension {
    @Override
    public void beforeInjector(StartupContext startupContext) {
        startupContext.getConfigProvider().addConfig("forest.http.port", "" + FreePortFinder.findFreeLocalPort());
    }
}
