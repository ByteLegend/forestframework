package io.forestframework;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.Element;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.TypeConverterBinding;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GuiceTest {
    public static void main(String[] args) {
        Injector injector = new InjectorDecorator(Guice.createInjector());
        injector.getInstance(A.class);
    }
}

class InjectorDecorator implements Injector {
    Injector delegate;

    public InjectorDecorator(Injector delegate) {
        this.delegate = delegate;
    }

    @Override
    public void injectMembers(Object instance) {
        throw new RuntimeException();
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
        throw new RuntimeException();
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
        throw new RuntimeException();
    }

    @Override
    public Map<Key<?>, Binding<?>> getBindings() {
        throw new RuntimeException();
    }

    @Override
    public Map<Key<?>, Binding<?>> getAllBindings() {
        throw new RuntimeException();
    }

    @Override
    public <T> Binding<T> getBinding(Key<T> key) {
        throw new RuntimeException();
    }

    @Override
    public <T> Binding<T> getBinding(Class<T> type) {
        throw new RuntimeException();
    }

    @Override
    public <T> Binding<T> getExistingBinding(Key<T> key) {
        throw new RuntimeException();
    }

    @Override
    public <T> List<Binding<T>> findBindingsByType(TypeLiteral<T> type) {
        throw new RuntimeException();
    }

    @Override
    public <T> Provider<T> getProvider(Key<T> key) {
        throw new RuntimeException();
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        throw new RuntimeException();
    }

    @Override
    public <T> T getInstance(Key<T> key) {
        throw new RuntimeException();
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return delegate.getInstance(type);
    }

    @Override
    public Injector getParent() {
        throw new RuntimeException();
    }

    @Override
    public Injector createChildInjector(Iterable<? extends Module> modules) {
        throw new RuntimeException();
    }

    @Override
    public Injector createChildInjector(Module... modules) {
        throw new RuntimeException();
    }

    @Override
    public Map<Class<? extends Annotation>, Scope> getScopeBindings() {
        throw new RuntimeException();
    }

    @Override
    public Set<TypeConverterBinding> getTypeConverterBindings() {
        throw new RuntimeException();
    }

    @Override
    public List<Element> getElements() {
        throw new RuntimeException();
    }

    @Override
    public Map<TypeLiteral<?>, List<InjectionPoint>> getAllMembersInjectorInjectionPoints() {
        throw new RuntimeException();
    }
}

class B {

}

class A {
    @Inject
    public A(@Named("a") B b) {
    }
}
