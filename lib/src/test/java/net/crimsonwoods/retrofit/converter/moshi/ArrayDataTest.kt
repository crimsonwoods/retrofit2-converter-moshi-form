package net.crimsonwoods.retrofit.converter.moshi

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squareup.moshi.Moshi
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import net.crimsonwoods.retrofit.converter.moshi.api.Item
import net.crimsonwoods.retrofit.converter.moshi.api.ItemId
import net.crimsonwoods.retrofit.converter.moshi.api.RetrofitService
import net.crimsonwoods.retrofit.converter.moshi.api.UserId
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URLEncoder

@RunWith(AndroidJUnit4::class)
class ArrayDataTest {
    private val mockWebServer = MockWebServer()
    private val moshi = Moshi.Builder()
        .add(ItemId::class.java, ItemId.Adapter)
        .build()
    private lateinit var service: RetrofitService

    @Before
    fun setup() {
        mockWebServer.start()
        service = RetrofitService(url = mockWebServer.url("/"), moshi = moshi)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun nestedData() = runBlocking {
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(200)
            setBody("""{}""")
        })

        service.addItems(
            userId = UserId(1),
            items = listOf(Item(id = 1, name = "item1", description = "1st item")),
        )

        assertEquals(1, mockWebServer.requestCount)
        val actual = mockWebServer.takeRequest()
        assertEquals("PUT", actual.method)
        assertEquals("/user/1/items", actual.path)
        assertEquals("application/x-www-form-urlencoded", actual.headers["Content-Type"])
        assertEquals(
            listOf(
                """${"items[0][id]".encode()}=1""",
                """${"items[0][name]".encode()}=item1""",
                """${"items[0][description]".encode()}=${"1st item".encode()}""",
            ),
            actual.body.readUtf8().split('&')
        )
    }

    private fun String.encode(): String {
        // URLEncoder converts ' ' to '+' because it is according to RFC1738.
        // OkHttp's HttpUrl converts ' ' to "%20" because it is according to RFC3986.
        // And RFC3986 is newer than RFC1738.
        // So, ' ' should be converted to "%20".
        return URLEncoder.encode(this, "utf-8").replace("+", "%20")
    }
}
