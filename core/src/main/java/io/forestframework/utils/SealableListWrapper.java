package io.forestframework.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

class SealableListWrapper<E> implements List<E>, Sealable {
    private final List<E> delegate;
    private Supplier<Boolean> sealState;

    public SealableListWrapper(List<E> delegate, Supplier<Boolean> sealState) {
        this.sealState = sealState;
        this.delegate = delegate;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new SealableIteratorWrapper<>(delegate.iterator(), sealState);
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean add(E e) {
        checkSealed();
        return delegate.add(e);
    }

    @Override
    public boolean remove(Object o) {
        checkSealed();
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        checkSealed();
        return delegate.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        checkSealed();
        return delegate.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        checkSealed();
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        checkSealed();
        return delegate.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        checkSealed();
        delegate.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        checkSealed();
        delegate.sort(c);
    }

    @Override
    public void clear() {
        checkSealed();
        delegate.clear();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public E get(int index) {
        return delegate.get(index);
    }

    @Override
    public E set(int index, E element) {
        checkSealed();
        return delegate.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        checkSealed();
        delegate.add(index, element);
    }

    @Override
    public E remove(int index) {
        checkSealed();
        return delegate.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator() {
        return new SealableListIteratorWrapper<>(delegate.listIterator(), sealState);
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator(int index) {
        return new SealableListIteratorWrapper<>(delegate.listIterator(index), sealState);
    }

    @NotNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return new SealableListWrapper<>(delegate.subList(fromIndex, toIndex), sealState);
    }

    @Override
    public Spliterator<E> spliterator() {
        return delegate.spliterator();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        checkSealed();
        return delegate.removeIf(filter);
    }

    @Override
    public Stream<E> stream() {
        return delegate.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return delegate.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        delegate.forEach(action);
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

