package org.forestframework;

import com.google.inject.Key;
import com.google.inject.internal.UnsafeInstanceBindingImpl;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.forestframework.annotation.Config;
import org.forestframework.config.ConfigProvider;

import java.util.concurrent.Callable;

class Unsafe {
    static void instrumentGuice(ConfigProvider configProvider) {
        InheritingStateGetExplicitBindingInterceptor.configProvider = configProvider;
        ByteBuddyAgent.install();
        new AgentBuilder.Default().type(ElementMatchers.named("com.google.inject.internal.InheritingState"))
                .transform((builder, typeDescription, classLoader, module) -> builder
                        .method(ElementMatchers.named("getExplicitBinding"))
                        .intercept(MethodDelegation.to(InheritingStateGetExplicitBindingInterceptor.class))
                )
                .installOnByteBuddyAgent();
    }

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
