package io.forestframework.utils;

public class Pair<K, V> {
    private final K left;
    private final V right;

    private Pair(K left, V right) {
        this.left = left;
        this.right = right;
    }

    public static <K, V> Pair<K, V> of(K left, V right) {
        return new Pair<>(left, right);
    }

    public K getLeft() {
        return left;
    }

    public V getRight() {
        return right;
    }
}
