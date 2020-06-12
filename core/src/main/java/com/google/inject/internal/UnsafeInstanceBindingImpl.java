package com.google.inject.internal;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.internal.util.SourceProvider;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.InstanceBinding;

import java.util.Collections;
import java.util.Set;

public class UnsafeInstanceBindingImpl extends BindingImpl<Object> implements InstanceBinding<Object> {
    private final Object instance;

    public UnsafeInstanceBindingImpl(Key key, Object instance) {
        super(SourceProvider.UNKNOWN_SOURCE, key, Scoping.EAGER_SINGLETON);
        this.instance = instance;
    }

    @Override
    public Object getInstance() {
        return instance;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public <V> V acceptTargetVisitor(BindingTargetVisitor<? super Object, V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void applyTo(Binder binder) {
        binder.withSource(getSource()).bind(getKey()).toInstance(instance);
    }

    @Override
    public Set<Dependency<?>> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    public InternalFactory<?> getInternalFactory() {
        return new ConstantFactory(Initializables.of(instance));
    }
}


