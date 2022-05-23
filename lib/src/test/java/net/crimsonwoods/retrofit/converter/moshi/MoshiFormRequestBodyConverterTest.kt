package net.crimsonwoods.retrofit.converter.moshi

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squareup.moshi.JsonClass
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

    @JsonClass(generateAdapter = true)
    data class InputDouble(
        val value: Double,
        val array: List<Double>,
    )

    @JsonClass(generateAdapter = true)
    data class InputLong(
        val value: Long,
        val array: List<Long>,
    )

    @Test
    fun double() {
        val converter = MoshiFormRequestBodyConverter(
            adapter = moshi.adapter(InputDouble::class.java),
            hasArrayIndex = false,
        )

        data class Input(
            val input: InputDouble,
            val expected: List<Pair<String, String>>,
        )

        val inputs = listOf(
            Input(
                input = InputDouble(0.1, listOf(0.2)),
                expected = listOf("value" to "0.1", "array[]" to "0.2")
            ),
            Input(
                input = InputDouble(Double.MAX_VALUE, listOf(Double.MIN_VALUE)),
                expected = listOf(
                    "value" to "${Double.MAX_VALUE}",
                    "array[]" to "${Double.MIN_VALUE}"
                )
            ),
        )

        inputs.forEach { input ->
            val actual = converter.convert(input.input)
            assertNotNull(actual)
            assertTrue(actual is FormBody)
            assertEquals(input.expected.size, actual.size)
            assertEquals(
                input.expected,
                (0 until actual.size).map { index -> actual.name(index) to actual.value(index) }
            )
        }
    }

    @Test
    fun long() {
        val converter = MoshiFormRequestBodyConverter(
            adapter = moshi.adapter(InputLong::class.java),
            hasArrayIndex = false,
        )

        data class Input(
            val input: InputLong,
            val expected: List<Pair<String, String>>,
        )

        val inputs = listOf(
            Input(
                input = InputLong(1L, listOf(2L)),
                expected = listOf("value" to "1", "array[]" to "2")
            ),
            Input(
                input = InputLong(Long.MAX_VALUE, listOf(Long.MIN_VALUE)),
                expected = listOf("value" to "${Long.MAX_VALUE}", "array[]" to "${Long.MIN_VALUE}")
            ),
        )

        inputs.forEach { input ->
            val actual = converter.convert(input.input)
            assertNotNull(actual)
            assertTrue(actual is FormBody)
            assertEquals(input.expected.size, actual.size)
            assertEquals(
                input.expected,
                (0 until actual.size).map { index -> actual.name(index) to actual.value(index) }
            )
        }
    }

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
    fun emptyArray() {
        val converter = MoshiFormRequestBodyConverter(
            adapter = moshi.adapter(Array<String>::class.java),
            hasArrayIndex = false,
        )

        val actual = converter.convert(arrayOf())
        assertNotNull(actual)
        assertTrue(actual is FormBody)
        assertEquals(0, actual.size)
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
