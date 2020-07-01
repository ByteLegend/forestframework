package io.forestframework.core.http.param;

public interface Form<T> {
    T getData();

    boolean hasErrors();
}
