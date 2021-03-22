package io.forestframework.core.ext

import com.github.blindpirate.annotationmagic.Extends
import io.forestframework.core.internal.ExtensionScanner
import io.forestframework.ext.api.After
import io.forestframework.ext.api.Before
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.WithExtensions
import io.forestframework.ext.core.AutoComponentScanExtension
import io.forestframework.ext.core.IncludeComponents
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testfixtures.DisableAutoScanExtension
import org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ExtensionWithDefaultConstructor : Extension

class ExtensionWithAnnotationConstructor(
    val annotation: EnableExtensionWithAnnotationConstructor
) : Extension

@Extends(WithExtensions::class)
@WithExtensions(extensions = [ExtensionWithAnnotationConstructor::class])
annotation class EnableExtensionWithAnnotationConstructor(val number: Int)

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

@IncludeComponents(classes = [AA::class])
class TestApp4

@IncludeComponents(classes = [BB::class])
class TestApp5

@WithExtensions(extensions = [AutoComponentScanExtension::class])
@DisableAutoScan
@IncludeComponents(classes = [AA::class])
class TestApp6

class ExtensionScannerTest {
    @Test
    fun `can deduplicate extension classes`() {
        assertEquals(1, ExtensionScanner.scan(TestApp1::class.java.annotations.toList()).size)
    }

    @Test
    fun `can reorder extension classes`() {
        assertEquals(listOf("A", "B", "C", "F", "E", "D"),
            ExtensionScanner.scan(TestApp2::class.java.annotations.toList()).map { it.javaClass.simpleName })
    }

    @Test
    fun `can report error for cyclic extension dependencies`() {
        val exception = assertThrows<Exception> {
            ExtensionScanner.scan(TestApp3::class.java.annotations.toList())
        }
        assertThat(
            getStackTrace(exception),
            containsString("class io.forestframework.core.ext.BB <- class io.forestframework.core.ext.DD")
        )
        assertThat(
            getStackTrace(exception),
            containsString("class io.forestframework.core.ext.CC <- class io.forestframework.core.ext.BB")
        )
        assertThat(
            getStackTrace(exception),
            containsString("class io.forestframework.core.ext.DD <- class io.forestframework.core.ext.CC")
        )
    }

    @Test
    fun `can instantiate with annotation constructor`() {
        assertEquals(
            42,
            (ExtensionScanner.scan(TestApp1::class.java.annotations.toList())[0] as ExtensionWithAnnotationConstructor).annotation.number
        )
    }

    @Test
    fun `repeatable extensions are not deduplicated`() {
        assertEquals(
            2,
            (ExtensionScanner.scan(TestApp4::class.java.annotations.toList() + TestApp5::class.java.annotations.toList())).size
        )
    }

    @Test
    fun `realworld reorder test`() {
        assertEquals(
            listOf(
                AutoComponentScanExtension::class.java,
                DisableAutoScanExtension::class.java,
                IncludeComponents.IncludeComponentExtension::class.java
            ),
            ExtensionScanner.scan(TestApp6::class.java.annotations.toList()).map { it.javaClass }
        )
    }
}
