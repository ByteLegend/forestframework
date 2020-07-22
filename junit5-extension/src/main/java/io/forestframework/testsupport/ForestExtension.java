package io.forestframework.testsupport;

import io.forestframework.core.Application;
import io.forestframework.core.Forest;
import io.forestframework.ext.api.Extension;
import io.forestframework.testsupport.utils.FreePortFinder;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.forestframework.utils.ComponentScanUtils.getApplicationAnnotation;
import static java.util.Arrays.asList;

/**
 * The JUnit 5 extension which provides support of:
 *
 * <ul>
 *     <li>Test application startup and cleanup.</li>
 *     <li>Auto detect available HTTP port to listen.</li>
 *     <li>Inject application instances to test instances.</li>
 *     <li>Replace the dependencies in test application with mocked implementations (Mockk and Mockito).</li>
 * </ul>
 */
public class ForestExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor, BeforeEachCallback {
    private Application application;

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        this.application.close();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getTestClass().get();
        ForestTest annotation = testClass.getAnnotation(ForestTest.class);
        this.application = Forest.run(annotation.appClass(), getExtensionsInTest(annotation), getConfigsInTest(annotation));
    }

    private Map<String, String> getConfigsInTest(ForestTest annotation) {
        Map<String, String> configs = new HashMap<>();
        for (String extraConfig : annotation.extraConfigs()) {
            if (StringUtils.countMatches(extraConfig, '=') < 1) {
                throw new RuntimeException("Invalid config: " + extraConfig);
            }

            configs.put(StringUtils.substringBefore(extraConfig, "="), StringUtils.substringAfter(extraConfig, "="));
        }
        if (!configs.containsKey("forest.http.port")) {
            configs.put("forest.http.port", "" + FreePortFinder.findFreeLocalPort());
        }
        return configs;
    }

    private List<Class<? extends Extension>> getExtensionsInTest(ForestTest annotation) {
        List<Class<? extends Extension>> extensions = new ArrayList<>(asList(getApplicationAnnotation(annotation.appClass()).extensions()));
        extensions.addAll(asList(annotation.extraExtensions()));
        return extensions;
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        if (application != null) {
            application.getInjector().injectMembers(testInstance);
        }
    }
}
