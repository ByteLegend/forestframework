package org.forestframework.annotationmagic;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnnotationMagicTest {
    @Test
    public void catGetAllAnnotationsOfSameBaseAnnotation() {
        assertEquals("Base", AnnotationMagic.getOneAnnotationOnClass(TestClassWithBase.class, Base.class).value());
        assertEquals("Mid", AnnotationMagic.getOneAnnotationOnClass(TestClassWithMid.class, Base.class).value());
        assertEquals("Sub", AnnotationMagic.getOneAnnotationOnClass(TestClassWithSub.class, Base.class).value());
    }

    @Test
    public void realWorldAnnotationInheritanceTest() {
        assertEquals(HttpMethod.POST, AnnotationMagic.getOneAnnotationOnClass(TestClassWithRoute.class, Route.class).method());
        assertEquals("test", AnnotationMagic.getOneAnnotationOnClass(TestClassWithRoute.class, Route.class).path());

        assertEquals(HttpMethod.GET, AnnotationMagic.getOneAnnotationOnClass(TestClassWithGet.class, Route.class).method());
        assertEquals("get", AnnotationMagic.getOneAnnotationOnClass(TestClassWithGet.class, Route.class).path());

        assertEquals(HttpMethod.POST, AnnotationMagic.getOneAnnotationOnClass(TestClassWithPost.class, Route.class).method());
        assertEquals("post", AnnotationMagic.getOneAnnotationOnClass(TestClassWithPost.class, Route.class).path());

        assertEquals("socketjs", AnnotationMagic.getOneAnnotationOnClass(TestClassWithSocketJS.class, Route.class).path());

        assertEquals("intercept", AnnotationMagic.getOneAnnotationOnClass(TestClassWithIntercept.class, Intercept.class).path());
        assertEquals("intercept", AnnotationMagic.getOneAnnotationOnClass(TestClassWithIntercept.class, Route.class).path());
        assertEquals(HttpMethod.POST, AnnotationMagic.getOneAnnotationOnClass(TestClassWithIntercept.class, Intercept.class).method());
        assertEquals(HttpMethod.POST, AnnotationMagic.getOneAnnotationOnClass(TestClassWithIntercept.class, Route.class).method());
        assertEquals(InterceptType.AFTER_SUCCESS, AnnotationMagic.getOneAnnotationOnClass(TestClassWithIntercept.class, Intercept.class).type());

        assertEquals("prehandler", AnnotationMagic.getOneAnnotationOnClass(TestClassWithPreHandler.class, Intercept.class).path());
        assertEquals("prehandler", AnnotationMagic.getOneAnnotationOnClass(TestClassWithPreHandler.class, Route.class).path());
        assertEquals(HttpMethod.GET, AnnotationMagic.getOneAnnotationOnClass(TestClassWithPreHandler.class, Intercept.class).method());
        assertEquals(HttpMethod.GET, AnnotationMagic.getOneAnnotationOnClass(TestClassWithPreHandler.class, Route.class).method());
        assertEquals(InterceptType.PRE_HANDLER, AnnotationMagic.getOneAnnotationOnClass(TestClassWithPreHandler.class, Intercept.class).type());

        assertEquals("aftersuccess", AnnotationMagic.getOneAnnotationOnClass(TestClassWithAfterSuccess.class, Intercept.class).path());
        assertEquals("aftersuccess", AnnotationMagic.getOneAnnotationOnClass(TestClassWithAfterSuccess.class, Route.class).path());
        assertEquals(HttpMethod.POST, AnnotationMagic.getOneAnnotationOnClass(TestClassWithAfterSuccess.class, Intercept.class).method());
        assertEquals(HttpMethod.POST, AnnotationMagic.getOneAnnotationOnClass(TestClassWithAfterSuccess.class, Route.class).method());
        assertEquals(InterceptType.AFTER_SUCCESS, AnnotationMagic.getOneAnnotationOnClass(TestClassWithAfterSuccess.class, Intercept.class).type());
    }

    @Test
    public void reportErrorsWhenCircularInheritanceDetected() {
        Exception exception = assertThrows(Exception.class, () -> AnnotationMagic.getOneAnnotationOnClass(TestClassWithCircularAnnotation.class, CircularBase.class));
        assertTrue(exception.getMessage().contains("circular inheritance detected:"));
        assertTrue(exception.getMessage().contains("CircularMid"));
    }

    @Test
    public void reportErrorWhenMultipleAnnotationsWithSameBaseTypeFound() {
        Exception exception = assertThrows(Exception.class, () -> AnnotationMagic.getOneAnnotationOnClass(TestClassWithSameBaseType.class, Base.class));
        assertTrue(exception.getMessage().contains("Found more than one annotation on target class"));

        AnnotationMagic.getOneAnnotationOnClass(TestClassWithSameBaseType.class, Sub.class);
    }

    @Test
    public void instanceOfTest() {
        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClass(TestClassWithRoute.class, Route.class), Route.class));

        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClass(TestClassWithGet.class, Route.class), Route.class));
        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClass(TestClassWithGet.class, Get.class), Route.class));

        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClass(TestClassWithIntercept.class, Route.class), Route.class));
        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClass(TestClassWithIntercept.class, Intercept.class), Route.class));

        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClass(TestClassWithAfterSuccess.class, Route.class), Route.class));
        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClass(TestClassWithAfterSuccess.class, Intercept.class), Route.class));
        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClass(TestClassWithAfterSuccess.class, AfterSuccess.class), Route.class));
        assertTrue(AnnotationMagic.instanceOf(AnnotationMagic.getOneAnnotationOnClass(TestClassWithAfterSuccess.class, AfterSuccess.class), Intercept.class));
    }
}

@Base
@Sub
class TestClassWithSameBaseType {
}

enum HttpMethod {
    GET, POST
}

enum InterceptType {
    PRE_HANDLER,
    AFTER_SUCCESS
}

@Retention(RetentionPolicy.RUNTIME)
@interface Route {

    HttpMethod method() default HttpMethod.GET;

    String path() default "";
}


@Route(method = HttpMethod.POST, path = "test")
class TestClassWithRoute {

}

@Retention(RetentionPolicy.RUNTIME)
@Extends(Route.class)
@interface Get {
    String path() default "";
}

@Get(path = "get")
class TestClassWithGet {
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(Route.class)
@Route(method = HttpMethod.POST)
@interface Post {
    String path() default "";
}

@Post(path = "post")
class TestClassWithPost {
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(Route.class)
@interface SocketJS {
    String path() default "";
}

@SocketJS(path = "socketjs")
class TestClassWithSocketJS {
}


@Retention(RetentionPolicy.RUNTIME)
@Extends(Route.class)
@Route
@interface Intercept {
    InterceptType type() default InterceptType.PRE_HANDLER;

    HttpMethod method() default HttpMethod.GET;

    String path() default "";
}

@Intercept(type = InterceptType.AFTER_SUCCESS, method = HttpMethod.POST, path = "intercept")
class TestClassWithIntercept {
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(Intercept.class)
@interface PreHandler {
    String path() default "";

    HttpMethod method() default HttpMethod.GET;
}

@PreHandler(path = "prehandler")
class TestClassWithPreHandler {
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(Intercept.class)
@Intercept(type = InterceptType.AFTER_SUCCESS)
@interface AfterSuccess {
    String path() default "";

    HttpMethod method() default HttpMethod.GET;
}

@AfterSuccess(path = "aftersuccess", method = HttpMethod.POST)
class TestClassWithAfterSuccess {
}

// +--- Get/Post/Patch/Delete
// +--- StaticResource
// +--- SocketJS
// +--- SocketJSBridge
// +--- Intercept
//   +--- PreHandler
//   +--- AfterSuccess
//   +--- AfterFailure
//   +--- AfterCompletion

@Retention(RetentionPolicy.RUNTIME)
@Extends(CircularMid.class)
@interface CircularBase {
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(CircularBase.class)
@interface CircularMid {
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(CircularMid.class)
@interface CircularSub {
}

@CircularSub
class TestClassWithCircularAnnotation {
}

@Base
class TestClassWithBase {
}

@Mid
class TestClassWithMid {
}

@Sub
class TestClassWithSub {
}

@Retention(RetentionPolicy.RUNTIME)
@interface Base {
    String value() default "Base";
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(Base.class)
@interface Mid {
    String value() default "Mid";
}

@Retention(RetentionPolicy.RUNTIME)
@Extends(Mid.class)
@interface Sub {
    String value() default "Sub";
}
