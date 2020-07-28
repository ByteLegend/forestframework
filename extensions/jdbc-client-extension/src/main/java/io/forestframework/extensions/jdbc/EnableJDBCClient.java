package io.forestframework.extensions.jdbc;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.ext.api.EnableExtensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Extends(EnableExtensions.class)
@EnableExtensions(extensions = {JDBCClientExtension.class})
public @interface EnableJDBCClient {
}
