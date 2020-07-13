package io.forestframework.core;

import io.forestframework.core.config.ConfigProvider;
import io.forestframework.ext.core.AutoScanComponentsExtension;
import io.forestframework.ext.core.RoutingExtension;
import io.forestframework.utils.ComponentScanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static io.forestframework.utils.ComponentScanUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class Forest {
    public static void run(Class<?> appClass, String... args) {
        ConfigProvider configProvider = ConfigProvider.load();
        UnsafeHack.instrumentGuice(configProvider);
        new Application(appClass, Arrays.asList(getApplicationAnnotation(appClass).extensions()), configProvider).start();
    }
}

