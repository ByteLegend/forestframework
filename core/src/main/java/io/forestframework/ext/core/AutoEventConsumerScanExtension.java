package io.forestframework.ext.core;

import com.google.inject.Injector;
import io.forestframework.core.http.AbstractWebRequestHandler;
import io.forestframework.ext.api.After;
import io.forestframework.ext.api.ApplicationContext;
import io.forestframework.ext.api.ConsumeEvent;
import io.forestframework.ext.api.Extension;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.stream.Stream;

@API(status = API.Status.INTERNAL)
@After(classes = {AutoComponentScanExtension.class})
public class AutoEventConsumerScanExtension implements Extension {
    private EventConsumerHandler eventConsumerHandler;

    @Override
    public void configure(Injector injector) {
        eventConsumerHandler = injector.getInstance(EventConsumerHandler.class);
        ApplicationContext ac = injector.getInstance(ApplicationContext.class);
        ac.getComponents()
          .forEach(klass -> registerEventConsumers(injector, ac.getVertx(), klass));
    }

    private void registerEventConsumers(Injector injector, Vertx vertx, Class<?> component) {
        Stream.of(component.getMethods()).forEach(method -> {
            ConsumeEvent anno = method.getAnnotation(ConsumeEvent.class);
            if (anno != null) {
                vertx.eventBus().consumer(anno.value(), message -> {
                    eventConsumerHandler.handleMessage(message, component, method);
                });
            }
        });
    }
}

// TODO restructure this
@Singleton
class EventConsumerHandler extends AbstractWebRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumerHandler.class);
    @Inject
    public EventConsumerHandler(Vertx vertx, Injector injector) {
        super(vertx, injector);
    }

    void handleMessage(Message<Object> message, Class<?> component, Method method) {
        invokeMethod(injector.getInstance(component), method, new Object[]{message.body()}, false)
            .exceptionally(t -> {
                 LOGGER.error("", t);
                 return null;
            });
    }
}
