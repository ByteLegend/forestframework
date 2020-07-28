package io.forestframework.core.http.routing;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.core.http.HttpMethod;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Extends(Route.class)
@Route(methods = {HttpMethod.CONNECT})
public @interface Connect {
    String value();

    String regex();
}
