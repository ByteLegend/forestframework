package io.forestframework.core;

import com.github.blindpirate.annotationmagic.CompositeOf;

import javax.inject.Singleton;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CompositeOf({Singleton.class, com.google.inject.Singleton.class, Component.class})
public @interface SingletonComponent {
}
