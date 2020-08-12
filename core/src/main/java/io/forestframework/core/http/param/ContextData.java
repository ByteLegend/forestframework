package io.forestframework.core.http.param;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public @interface ContextData {
    String value();
}
