package io.forestframework.ext.core;

import com.google.inject.Injector;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.http.param.PathParam;
import io.forestframework.core.http.param.RoutingParameterResolver;
import io.forestframework.core.http.routing.Routing;
import io.forestframework.core.http.routing.RoutingType;
import io.forestframework.core.http.routing.Routings;
import io.forestframework.core.http.staticresource.StaticResource;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
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
 * explicitly via {@code @ForestApplication(extensions = StaticResourceExtension.class)}
 *
 * 2. Classpath scanning and wildcard route matching harms performance.
 *
 *
 * Also see {@link io.forestframework.core.http.staticresource.StaticResourceProcessor}
 */
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

    @Override
    public void beforeInjector(StartupContext startupContext) {
        startupContext.getConfigProvider().addDefaultOptions("forest.static.webroot", () -> "static");
        startupContext.getConfigProvider().addDefaultOptions("forest.static.webroots", ArrayList::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterInjector(Injector injector) {
        ConfigProvider configProvider = injector.getInstance(ConfigProvider.class);
        String webroot = configProvider.getInstance("forest.static.webroot", String.class);
        List<String> webroots = configProvider.getInstance("forest.static.webroots", List.class);

        webroots.add(webroot);

        Routings routings = injector.getInstance(Routings.class);
        for (String root : webroots) {
            configureOne(root, routings);
        }
    }

    private boolean rootPathNotConfigured(Routings routings) {
        return routings.getRouting(RoutingType.HANDLER).stream().noneMatch(it -> it.getPath().equals("/"));
    }

    private void configureOne(String webrootDir, Routings routings) {
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
                            if ("index.html".equals(staticResource.getName()) && rootPathNotConfigured(routings)) {
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
    private void registerResourceRouting(Routings routings, String webrootDir, File resourceFile) {
        routings.getRouting(RoutingType.HANDLER).add(
                new StaticResourceRouting("/" + resourceFile.getName(), webrootDir + "/" + resourceFile.getName(), GET_RESOURCE_FILE_METHOD));
    }

    private void registerRootPathRouting(Routings routings, String webrootDir) {
        routings.getRouting(RoutingType.HANDLER).add(
                new StaticResourceRouting("/", webrootDir + "/index.html", GET_RESOURCE_FILE_METHOD));
    }

    /**
     * static/js -> @GetStaticResource("/js/*")
     */
    private void registerDirectoryRouting(Routings routings, String webrootDir, File resourceDir) {
        routings.getRouting(RoutingType.HANDLER).add(
                new StaticResourceRouting("/" + resourceDir.getName() + "/*", webrootDir + "/" + resourceDir.getName() + "/", GET_RESOURCE_DIR_METHOD));
    }

    @StaticResource
    public String getResourceInDir(String resourceDirPath, @PathParam("*") String path) {
        return resourceDirPath + path;
    }

    @StaticResource
    public String getResourceFile(String resourceFilePath) {
        return resourceFilePath;
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
                return (RoutingParameterResolver<Object>) (routing, routingContext, paramIndex) -> mappedResourcePath;
            } else {
                return Routing.super.getParameterResolver(injector, index);
            }
        }
    }
}
