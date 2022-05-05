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
        assertEquals("[]", actual.name(0))
        assertEquals("012", actual.value(0))
        assertEquals("[]", actual.name(1))
        assertEquals("abcdefg", actual.value(1))
    }
}
