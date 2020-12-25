package io.forestframework.core.http.param;

import com.github.blindpirate.annotationmagic.Extends;
import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
@Extends(ParameterResolver.class)
@ParameterResolver(resolver = ContextDataParameterResolver.class)
public @interface ContextData {
    String value();
}
