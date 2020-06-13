package io.forestframework;

public interface Form<T> {
    T getData();

    boolean hasErrors();
}
