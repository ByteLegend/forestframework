package org.forestframework;

public interface Form<T> {
    T getData();

    boolean hasErrors();
}
