package org.forestframework.annotation;

import org.forestframework.ContentTypeAwareRequestBodyParser;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;

@Target({ METHOD, FIELD, CONSTRUCTOR, PARAMETER, TYPE_USE })
//@ArgumentResolvedBy(ContentTypeAwareRequestBodyParser.class)
public @interface RequestBody {
}
