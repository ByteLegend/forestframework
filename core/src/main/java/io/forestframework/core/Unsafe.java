package io.forestframework.core;

import com.google.inject.Key;
import com.google.inject.internal.UnsafeInstanceBindingImpl;
import io.forestframework.config.Config;
import io.forestframework.config.ConfigProvider;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Don't touch this. This is a hack for {@link Config} injection.
 */
class Unsafe {
    private static final Logger LOGGER = LoggerFactory.getLogger(Unsafe.class);

    static void instrumentGuice(ConfigProvider configProvider) {
        try {
            InheritingStateGetExplicitBindingInterceptor.configProvider = configProvider;
            ByteBuddyAgent.install();
            new AgentBuilder.Default().type(ElementMatchers.named("com.google.inject.internal.InheritingState"))
                    .transform((builder, typeDescription, classLoader, module) -> builder
                            .method(ElementMatchers.named("getExplicitBinding"))
                            .intercept(MethodDelegation.to(InheritingStateGetExplicitBindingInterceptor.class))
                    )
                    .installOnByteBuddyAgent();
        } catch (Throwable e) {
            LOGGER.debug("", e);
            LOGGER.warn("Can't instrument Guice, @Config injection will not be available.");
        }
    }

    /**
     * Must be safe to be recognized by bytebuddy.
     */
    public static class InheritingStateGetExplicitBindingInterceptor {
        private static ConfigProvider configProvider;

        @RuntimeType
        public static java.lang.Object cache(@SuperCall Callable<Object> call, @AllArguments java.lang.Object[] arguments) throws Exception {
            if (arguments.length == 1 && arguments[0].getClass() == Key.class) {
                Key key = (Key) arguments[0];
                if (key.getAnnotation() != null && key.getAnnotation().annotationType() == Config.class) {
                    return new UnsafeInstanceBindingImpl(key, configProvider.getInstance(((Config) key.getAnnotation()).value(), key.getTypeLiteral().getRawType()));
                }
            }
            return call.call();
        }
    }
}
