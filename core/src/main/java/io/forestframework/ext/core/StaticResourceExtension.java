package io.forestframework.ext.core;

import com.google.inject.Injector;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.param.PathParam;
import io.forestframework.core.http.param.RoutingParameterResolver;
import io.forestframework.core.http.routing.Routing;
import io.forestframework.core.http.routing.RoutingManager;
import io.forestframework.core.http.routing.RoutingType;
import io.forestframework.core.http.staticresource.StaticResource;
import io.forestframework.ext.api.After;
import io.forestframework.ext.api.ApplicationContext;
import io.forestframework.ext.api.Before;
import io.forestframework.ext.api.Extension;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Scan classpath and register all classpath:/static/* resources
 * as {@link io.forestframework.core.http.staticresource.GetStaticResource} routings.
 *
 * For example, classpath:/static/js/ directory will be registered as '/js/*'
 *
 * Specially, classpath:/static/index.html will be registered as '/' routing.
 *
 * The "static" directory name can be configured via "forest.static.webroot" and "forest.static.webroots"
 *
 * Note that existing routings will not be overwritten. For example, if your application
 * defines route path '/', the default classpath:/static/index.html will not be registered.
 *
 * This extension is not enabled by default due to two reasons:
 *
 * 1. We don't want the static resource handling to be "magic". You have to enable this feature
 * explicitly via {@code @WithStaticResource()}
 *
 * 2. Classpath scanning and wildcard route matching harms performance.
 *
 *
 * Also see {@link io.forestframework.core.http.staticresource.StaticResourceProcessor}
 */
@After(classes = AutoRoutingScanExtension.class)
@Before(classes = HttpServerExtension.class)
public class StaticResourceExtension implements Extension {
    private static final Method GET_RESOURCE_FILE_METHOD;
    private static final Method GET_RESOURCE_DIR_METHOD;

    static {
        try {
            GET_RESOURCE_FILE_METHOD = StaticResourceExtension.class.getMethod("getResourceFile", String.class);
            GET_RESOURCE_DIR_METHOD = StaticResourceExtension.class.getMethod("getResourceInDir", String.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final List<String> webroots;

    public StaticResourceExtension() {
        this(Collections.singletonList("static"));
    }

    public StaticResourceExtension(WithStaticResource withStaticResource) {
        this(getWebroots(withStaticResource));
    }

    private StaticResourceExtension(List<String> webroots) {
        this.webroots = webroots;
    }

    private static List<String> getWebroots(WithStaticResource withStaticResource) {
        List<String> webroots = new ArrayList<>();
        webroots.add(withStaticResource.webroot());
        webroots.addAll(Arrays.asList(withStaticResource.webroots()));
        return webroots;
    }

    @Override
    public void start(ApplicationContext applicationContext) {
        applicationContext.getConfigProvider().addDefaultOptions("forest.static.webroot", () -> "static");
        applicationContext.getConfigProvider().addDefaultOptions("forest.static.webroots", () -> webroots);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void configure(Injector injector) {
        ConfigProvider configProvider = injector.getInstance(ConfigProvider.class);
        String webroot = configProvider.getInstance("forest.static.webroot", String.class);
        LinkedHashSet<String> webroots = new LinkedHashSet<>(configProvider.getInstance("forest.static.webroots", List.class));

        webroots.add(webroot);

        RoutingManager routings = injector.getInstance(RoutingManager.class);

        boolean rootPathRegistered = routings.getRouting(RoutingType.HANDLER).stream()
                                             .anyMatch(it -> "/".equals(it.getPath()));

        for (String root : webroots) {
            configureOne(root, routings, rootPathRegistered);
        }
    }

    private void configureOne(String webrootDir, RoutingManager routings, boolean rootPatRegistered) {
        if (webrootDir.startsWith("/")) {
            webrootDir = webrootDir.substring(1);
        }
        if (webrootDir.endsWith("/")) {
            webrootDir = webrootDir.substring(0, webrootDir.length() - 1);
        }

        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources(webrootDir);
            while (resources.hasMoreElements()) {
                File resourceFile = new File(resources.nextElement().getPath());
                if (resourceFile.isDirectory()) {
                    File[] staticResources = resourceFile.listFiles();
                    if (staticResources == null) {
                        continue;
                    }
                    for (File staticResource : staticResources) {
                        if (staticResource.isDirectory()) {
                            registerDirectoryRouting(routings, webrootDir, staticResource);
                        } else {
                            registerResourceRouting(routings, webrootDir, staticResource);
                            if ("index.html".equals(staticResource.getName()) && !rootPatRegistered) {
                                registerRootPathRouting(routings, webrootDir);
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * static/index.html -> @GetStaticResource("/index.html")
     */
    private void registerResourceRouting(RoutingManager routings, String webrootDir, File resourceFile) {
        routings.getRouting(RoutingType.HANDLER).add(
            new StaticResourceRouting("/" + resourceFile.getName(), webrootDir + "/" + resourceFile.getName(), GET_RESOURCE_FILE_METHOD));
    }

    private void registerRootPathRouting(RoutingManager routings, String webrootDir) {
        routings.getRouting(RoutingType.HANDLER).add(new RootPathRouting(webrootDir + "/index.html"));
    }

    /**
     * static/js -> @GetStaticResource("/js/**")
     */
    private void registerDirectoryRouting(RoutingManager routings, String webrootDir, File resourceDir) {
        routings.getRouting(RoutingType.HANDLER).add(
            new StaticResourceRouting("/" + resourceDir.getName() + "/**", webrootDir + "/" + resourceDir.getName() + "/", GET_RESOURCE_DIR_METHOD));
    }

    @StaticResource
    public String getResourceInDir(String resourceDirPath, @PathParam("**") String path) {
        return resourceDirPath + path;
    }

    @StaticResource
    public String getResourceFile(String resourceFilePath) {
        return resourceFilePath;
    }

    public class RootPathRouting extends StaticResourceRouting {
        public RootPathRouting(String mappedResourcePath) {
            super("/", mappedResourcePath, GET_RESOURCE_FILE_METHOD);
        }
    }

    public class StaticResourceRouting implements Routing {
        private final String path;
        private final String mappedResourcePath;
        private final Method handlerMethod;

        public StaticResourceRouting(String path, String mappedResourcePath, Method handlerMethod) {
            this.path = path;
            this.handlerMethod = handlerMethod;
            this.mappedResourcePath = mappedResourcePath;
        }

        @Override
        public Method getHandlerMethod() {
            return handlerMethod;
        }

        @Override
        public Object getHandlerInstance(Injector injector) {
            return StaticResourceExtension.this;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public RoutingParameterResolver<?> getParameterResolver(Injector injector, int index) {
            if (index == 0) {
                // param 0 of getResourceInDir and getResourceFile
                return (RoutingParameterResolver<WebContext>) (routing, routingContext, paramIndex) -> mappedResourcePath;
            } else {
                return Routing.super.getParameterResolver(injector, index);
            }
        }
    }
}
