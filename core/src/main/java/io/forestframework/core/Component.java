package io.forestframework.core;

import com.github.blindpirate.annotationmagic.Extends;
import org.apiguardian.api.API;

import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@API(status = API.Status.EXPERIMENTAL, since = "0.3")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Extends(Singleton.class)
@Scope
public @interface Component {
}
