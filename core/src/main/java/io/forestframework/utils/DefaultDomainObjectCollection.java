package io.forestframework.utils;

/**
 * Not thread-safe.
 * @param <E>
 */
//@API(status = API.Status.INTERNAL, since = "0.1")
//public class DefaultDomainObjectCollection<E> implements DomainObjectCollection<E> {
//    private final Collection<E> delegate;
//    private final List<Consumer<? super E>> actions = new ArrayList<>();
//    private boolean sealed;
//
//    public DefaultDomainObjectCollection(Collection<E> delegate) {
//        this.delegate = delegate;
//    }
//
//    @Override
//    public void all(Consumer<? super E> action) {
//        actions.add(action);
//        forEach(action);
//    }
//
//    @Override
//    public int size() {
//        return delegate.size();
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return delegate.isEmpty();
//    }
//
//    @Override
//    public boolean contains(Object o) {
//        return delegate.contains(o);
//    }
//
//    @NotNull
//    @Override
//    public Iterator<E> iterator() {
//        return delegate.iterator();
//    }
//
//    @NotNull
//    @Override
//    public Object[] toArray() {
//        return delegate.toArray();
//    }
//
//    @NotNull
//    @Override
//    public <T> T[] toArray(@NotNull T[] a) {
//        return delegate.toArray(a);
//    }
//
//    private void checkSealed() {
//        if(sealed) {
//            throw new UnsupportedOperationException("The collection is unmodifiable.");
//        }
//    }
//
//    private void performActions(E e) {
//        actions.forEach(action -> action.accept(e));
//    }
//
//    @Override
//    public boolean add(E e) {
//        checkSealed();
//        performActions(e);
//        return delegate.add(e);
//    }
//
//    @Override
//    public boolean remove(Object o) {
//        checkSealed();
//        return delegate.remove(o);
//    }
//
//    @Override
//    public boolean containsAll(@NotNull Collection<?> c) {
//        return delegate.containsAll(c);
//    }
//
//    @Override
//    public boolean addAll(@NotNull Collection<? extends E> c) {
//        checkSealed();
//        c.forEach(this::performActions);
//        return delegate.addAll(c);
//    }
//
//    @Override
//    public boolean removeAll(@NotNull Collection<?> c) {
//        checkSealed();
//        return delegate.removeAll(c);
//    }
//
//    @Override
//    public boolean retainAll(@NotNull Collection<?> c) {
//        checkSealed();
//        return delegate.retainAll(c);
//    }
//
//    @Override
//    public void clear() {
//        checkSealed();
//        delegate.clear();
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        return delegate.equals(o);
//    }
//
//    @Override
//    public int hashCode() {
//        return delegate.hashCode();
//    }
//
//    @Override
//    public void forEach(Consumer<? super E> action) {
//        delegate.forEach(action);
//    }
//}
