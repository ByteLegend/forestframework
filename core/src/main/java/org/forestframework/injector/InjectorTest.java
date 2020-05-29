package org.forestframework.injector;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class InjectorTest {
    public static void main(String[] args) {
        Module module1 = new AbstractModule() {
            @Override
            protected void configure() {
                super.configure();
                bind(A.class).to(A1.class);
            }
        };

        Module module2 = new AbstractModule() {
            @Override
            protected void configure() {
                super.configure();
                bind(A.class).to(A2.class);
            }
        };

        Module module3 = new AbstractModule() {
            @Override
            protected void configure() {
                super.configure();
                bind(A.class).to(A3.class);
            }
        };

        Injector injector = Guice.createInjector(
                Modules.override(
                        Modules.override(module1).with(module2)
                ).with(module3)
        );
        System.out.println(injector.getInstance(A.class));
    }
}

interface A {
}

class A1 implements A {
}

class A2 implements A {
}

class A3 implements A {
}

