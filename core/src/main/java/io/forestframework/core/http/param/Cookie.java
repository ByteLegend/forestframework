package io.forestframework.core.http.param;

import com.github.blindpirate.annotationmagic.Extends;
import org.apiguardian.api.API;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@API(status = API.Status.EXPERIMENTAL, since = "0.1")
@Retention(RetentionPolicy.RUNTIME)
@Extends(ParameterResolver.class)
@ParameterResolver(resolver = CookieParameterResolver.class)
public @interface Cookie {
    String value();
}
