package io.forestframework.testsupport;

import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.internal.ExtensionScanner;
import io.forestframework.ext.api.DefaultApplicationContext;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class ForestExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor {
    private DefaultApplicationContext applicationContext;

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        ConfigProvider configProvider = ConfigProvider.load();
        applicationContext = createStartupContext(context.getTestClass().get(), configProvider);
        applicationContext.start();
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        if (applicationContext != null) {
            applicationContext.getInjector().injectMembers(testInstance);
        }
    }

    private static List<Annotation> scanAnnotations(Class<?> appClass, Class<?> testClass) {
        List<Annotation> result = new ArrayList<>(Arrays.asList(appClass.getAnnotations()));
        while (testClass != null) {
            result.addAll(Arrays.asList(testClass.getAnnotations()));
            testClass = testClass.getSuperclass();
        }
        return result;
    }

    private static DefaultApplicationContext createStartupContext(Class<?> testClass, ConfigProvider configProvider) {
        ForestIntegrationTest forestTestAnno = testClass.getAnnotation(ForestIntegrationTest.class);
        if (forestTestAnno == null) {
            throw new IllegalArgumentException("Test class must be annotated with @ForestTest!");
        }

        List<Annotation> annotations = scanAnnotations(forestTestAnno.appClass(), testClass);
        return new DefaultApplicationContext(
            Vertx.vertx(),
            forestTestAnno.appClass(),
            configProvider,
            ExtensionScanner.scan(annotations));
    }
}
