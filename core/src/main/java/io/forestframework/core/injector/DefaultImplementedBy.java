package io.forestframework.core.injector;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is just a marker annotation which guides readers to the correct implementation class,
 * i.e. it <em>doesn't</em> do any real work as Guice {@link com.google.inject.ImplementedBy}.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface DefaultImplementedBy {
    /**
     * The default implementation class of marked interface.
     */
    Class<?> value();
}
