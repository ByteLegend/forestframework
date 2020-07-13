package io.forestframework.testsupport;

import io.forestframework.core.Application;
import io.forestframework.core.ForestApplication;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.utils.ComponentScanUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.util.Arrays;

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
        Class<?> appClass = annotation.getClass();

        ForestApplication forestApplication = ComponentScanUtils.getApplicationAnnotation(appClass);
        ConfigProvider configProvider = ConfigProvider.load();
        this.application = new Application(appClass, Arrays.asList(forestApplication.extensions()), configProvider);
        this.application.start();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        application.getInjector().injectMembers(testInstance);
    }
}
