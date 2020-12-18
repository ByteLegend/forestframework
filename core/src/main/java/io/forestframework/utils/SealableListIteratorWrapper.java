package io.forestframework.utils;

import java.util.ListIterator;
import java.util.function.Supplier;

public class SealableListIteratorWrapper<E> implements ListIterator<E>, Sealable {
    private final Supplier<Boolean> sealState;
    private final ListIterator<E> delegate;

    public SealableListIteratorWrapper(ListIterator<E> delegate, Supplier<Boolean> sealState) {
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
    public boolean hasPrevious() {
        return delegate.hasPrevious();
    }

    @Override
    public E previous() {
        return delegate.previous();
    }

    @Override
    public int nextIndex() {
        return delegate.nextIndex();
    }

    @Override
    public int previousIndex() {
        return delegate.previousIndex();
    }

    @Override
    public void remove() {
        checkSealed();
        delegate.remove();
    }

    @Override
    public void set(E e) {
        checkSealed();
        delegate.remove();
    }

    @Override
    public void add(E e) {
        checkSealed();
        delegate.add(e);
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
