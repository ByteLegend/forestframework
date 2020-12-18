package io.forestframework.utils;

import java.util.Iterator;
import java.util.function.Supplier;

public class SealableIteratorWrapper<E> implements Iterator<E>, Sealable {
    private final Supplier<Boolean> sealState;
    private final Iterator<E> delegate;

    public SealableIteratorWrapper(Iterator<E> delegate, Supplier<Boolean> sealState) {
        this.sealState = sealState;
        this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public E next() {
        return delegate.next();
    }

    @Override
    public void remove() {
        checkSealed();
        delegate.remove();
    }

    @Override
    public void seal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSealed() {
        return sealState.get();
    }
}
