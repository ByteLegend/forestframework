package io.forestframework.core

import com.github.blindpirate.annotationmagic.Extends
import io.forestframework.core.internal.ExtensionScanner
import io.forestframework.ext.api.After
import io.forestframework.ext.api.Before
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.WithExtensions
import org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ExtensionWithDefaultConstructor : Extension

class ExtensionWithAnnotationConstructor(
    val annotation: EnableExtensionWithAnnotationConstructor
) : Extension

@Extends(WithExtensions::class)
@WithExtensions(extensions = [ExtensionWithAnnotationConstructor::class])
annotation class EnableExtensionWithAnnotationConstructor(val number: Int)

@WithExtensions(extensions = [ExtensionWithAnnotationConstructor::class])
@EnableExtensionWithAnnotationConstructor(42)
class TestApp1

// A -> B
// B -> C
// C -> D
// A -> E
// E -> D
// C -> F
// F -> E
@Before(classes = [B::class])
class A : Extension
class B : Extension

@After(classes = [B::class])
@Before(classes = [D::class, F::class])
class C : Extension

@After(classes = [E::class])
class D : Extension

@Before(classes = [D::class])
class E : Extension

@Before(classes = [E::class])
class F : Extension

@WithExtensions(extensions = [A::class, B::class, C::class, D::class, E::class, F::class])
class TestApp2

// AA -> BB
// BB -> CC
// CC -> DD
// DD -> BB

@Before(classes = [BB::class])
class AA : Extension

@Before(classes = [CC::class])
class BB : Extension

@Before(classes = [DD::class])
class CC : Extension

@Before(classes = [BB::class])
class DD : Extension

@WithExtensions(extensions = [AA::class, BB::class, CC::class, DD::class])
class TestApp3

class ExtensionScannerTest {
    @Test
    fun `can deduplicate extension classes`() {
        Assertions.assertEquals(1, ExtensionScanner.scan(TestApp1::class.java.annotations.toList()).size)
    }

    @Test
    fun `can reorder extension classes`() {
        Assertions.assertEquals(listOf("A", "B", "C", "F", "E", "D"),
            ExtensionScanner.scan(TestApp2::class.java.annotations.toList()).map { it.javaClass.simpleName })
    }

    @Test
    fun `can report error for cyclic extension dependencies`() {
        val exception = assertThrows<Exception> {
            ExtensionScanner.scan(TestApp3::class.java.annotations.toList())
        }
        assertThat(getStackTrace(exception),
            containsString("class io.forestframework.core.DD -> class io.forestframework.core.BB"))
        assertThat(getStackTrace(exception),
            containsString("class io.forestframework.core.BB -> class io.forestframework.core.CC"))
        assertThat(getStackTrace(exception),
            containsString("class io.forestframework.core.CC -> class io.forestframework.core.DD"))
    }

    @Test
    fun `can instantiate with annotation constructor`() {
        Assertions.assertEquals(42,
            (ExtensionScanner.scan(TestApp1::class.java.annotations.toList())[0] as ExtensionWithAnnotationConstructor).annotation.number)
    }
}
