package io.forestframework.testsupport;

import io.forestframework.core.Application;
import io.forestframework.core.Forest;
import io.forestframework.testsupport.utils.FreePortFinder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

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
        Class<?> appClass = annotation.appClass();

        overwritePortPropertyIfNecessary();
        this.application = Forest.run(appClass, new String[0]);
        this.application.start();
    }

    private void overwritePortPropertyIfNecessary() {
        String currentPortProperty = System.getProperty("forest.http.port");
        if (currentPortProperty == null || "0".equals(currentPortProperty)) {
            System.setProperty("forest.http.port", "" + FreePortFinder.findFreeLocalPort());
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        application.getInjector().injectMembers(testInstance);
    }
}
