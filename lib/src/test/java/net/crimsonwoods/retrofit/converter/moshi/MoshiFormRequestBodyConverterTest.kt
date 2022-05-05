package net.crimsonwoods.retrofit.converter.moshi

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squareup.moshi.Moshi
import okhttp3.FormBody
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MoshiFormRequestBodyConverterTest {
    private val moshi = Moshi.Builder().build()

    @Test
    fun arrayOnly() {
        val converter = MoshiFormRequestBodyConverter(
            adapter = moshi.adapter(Array<String>::class.java),
            hasArrayIndex = false,
        )

        val actual = converter.convert(arrayOf("012", "abcdefg"))
        assertNotNull(actual)
        assertTrue(actual is FormBody)
        assertEquals(2, actual.size)
        assertEquals("[]", actual.name(0))
        assertEquals("012", actual.value(0))
        assertEquals("[]", actual.name(1))
        assertEquals("abcdefg", actual.value(1))
    }

    @Test
    fun valueOnly() {

        val inputs = listOf<ValueOnlyInput<*>>(
            ValueOnlyInput.S("test"),
            ValueOnlyInput.N(123L),
            ValueOnlyInput.B(true),
            ValueOnlyInput.Null,
        )

        inputs.forEach { input ->
            val (actual, expected) = when (input) {
                is ValueOnlyInput.S -> {
                    val converter = MoshiFormRequestBodyConverter(
                        adapter = moshi.adapter(input.type),
                        hasArrayIndex = false,
                    )

                    converter.convert(input.value) to input.expected
                }
                is ValueOnlyInput.N -> {
                    val converter = MoshiFormRequestBodyConverter(
                        adapter = moshi.adapter(input.type),
                        hasArrayIndex = false,
                    )

                    converter.convert(input.value) to input.expected
                }
                is ValueOnlyInput.B -> {
                    val converter = MoshiFormRequestBodyConverter(
                        adapter = moshi.adapter(input.type),
                        hasArrayIndex = false,
                    )

                    converter.convert(input.value) to input.expected
                }
                is ValueOnlyInput.Null -> {
                    val converter = MoshiFormRequestBodyConverter(
                        adapter = moshi.adapter(input.type).nullSafe(),
                        hasArrayIndex = false,
                    )

                    converter.convert(input.value) to input.expected
                }
            }

            assertNotNull(actual)
            assertTrue(actual is FormBody)
            assertEquals(1, actual.size)
            assertEquals("", actual.name(0))
            assertEquals(expected, actual.value(0))
        }
    }

    sealed interface ValueOnlyInput<T : Any> {
        val type: Class<T>
        val value: T?
        val expected: String

        data class S(private val v: String) : ValueOnlyInput<String> {
            override val type: Class<String>
                get() = String::class.java

            override val value: String
                get() = v

            override val expected: String
                get() = v
        }

        data class N(private val v: Long) : ValueOnlyInput<Long> {
            override val type: Class<Long>
                get() = Long::class.java

            override val value: Long
                get() = v

            override val expected: String
                get() = v.toString()
        }

        data class B(private val v: Boolean) : ValueOnlyInput<Boolean> {
            override val type: Class<Boolean>
                get() = Boolean::class.java

            override val value: Boolean
                get() = v

            override val expected: String
                get() = v.toString()
        }

        object Null : ValueOnlyInput<String> {
            override val type: Class<String>
                get() = String::class.java

            override val value: String?
                get() = null

            override val expected: String
                get() = "null"
        }
    }
}
