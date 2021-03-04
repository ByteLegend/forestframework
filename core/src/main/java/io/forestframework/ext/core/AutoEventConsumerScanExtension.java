package io.forestframework.ext.core;

import com.google.inject.Injector;
import io.forestframework.core.http.AbstractWebRequestHandler;
import io.forestframework.ext.api.After;
import io.forestframework.ext.api.ApplicationContext;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.OnEvent;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
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
            OnEvent anno = method.getAnnotation(OnEvent.class);
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

    private Object[] resolveParameters(Message<Object> message, Method method) {
        if (method.getParameterCount() == 0) {
            return new Object[0];
        } else if (method.getParameterCount() == 1) {
            Class<?> parameterType = method.getParameterTypes()[0];
            if (isContinuation(parameterType)) {
                return new Object[1];
            } else {
                return new Object[]{message.body()};
            }
        } else if (method.getParameterCount() == 2 && isContinuation(method.getParameterTypes()[1])) {
            return new Object[]{message.body(), null};
        }
        throw new IllegalArgumentException("We only support method with 1 param, but you have: " + method);
    }

    void handleMessage(Message<Object> message, Class<?> component, Method method) {
        invokeMethod(injector.getInstance(component), method, resolveParameters(message, method), false)
            .whenComplete((Object ret, Throwable t) -> {
                try {
                    if (t != null) {
                        LOGGER.error("Error invoking handler " + message.address(), t);
                    }
                    if (message.replyAddress() != null) {
                        if (t != null) {
                            message.reply(t.getMessage(), new DeliveryOptions().addHeader("type", "error"));
                        } else if (ret == null || "kotlin.Unit".equals(ret.getClass().getName())) {
                            message.reply(null, new DeliveryOptions().addHeader("type", "returnValue"));
                        } else {
                            message.reply(ret, new DeliveryOptions().addHeader("type", "returnValue"));
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.error("", e);
                }
            });
    }
}
