package io.forestframework.testsupport;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import io.forestframework.core.Application;
import io.forestframework.core.Forest;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.ext.api.EnableExtensions;
import io.forestframework.ext.api.StartupContext;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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
    private Application application;

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        this.application.close();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        ConfigProvider configProvider = ConfigProvider.load();
        application = Forest.run(createStartupContext(context.getTestClass().get(), configProvider));
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        if (application != null) {
            application.getInjector().injectMembers(testInstance);
        }
    }

    private static List<EnableExtensions> scanEnableExtensions(Class<?> appClass, Class<?> testClass) {
        List<EnableExtensions> annotationsOnAppClass = AnnotationMagic.getAnnotationsOnClass(appClass, EnableExtensions.class);
        List<EnableExtensions> annotationsOnTestClass = AnnotationMagic.getAnnotationsOnClass(testClass, EnableExtensions.class);

        List<EnableExtensions> result = new ArrayList<>();

        int testAnnoIterationIndex = 0;
        for (; testAnnoIterationIndex < annotationsOnTestClass.size(); ++testAnnoIterationIndex) {
            EnableExtensions anno = annotationsOnTestClass.get(testAnnoIterationIndex);
            if (AnnotationMagic.instanceOf(anno, ForestTest.class)) {
                break;
            } else {
                result.add(anno);
            }
        }

        result.addAll(annotationsOnAppClass);

        IntStream.range(testAnnoIterationIndex, annotationsOnTestClass.size()).mapToObj(annotationsOnTestClass::get).forEach(result::add);
        return result;
    }

    private static StartupContext createStartupContext(Class<?> testClass, ConfigProvider configProvider) {
        ForestTest forestTestAnno = testClass.getAnnotation(ForestTest.class);
        if (forestTestAnno == null) {
            throw new IllegalArgumentException("Test class must be annotated with @ForestTest!");
        }

        List<EnableExtensions> annotations = scanEnableExtensions(forestTestAnno.appClass(), testClass);

        return new TestApplicationStartupContext(Vertx.vertx(), forestTestAnno.appClass(), configProvider, annotations);
    }
}
