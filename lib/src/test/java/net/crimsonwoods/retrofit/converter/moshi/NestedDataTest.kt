package net.crimsonwoods.retrofit.converter.moshi

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import net.crimsonwoods.retrofit.converter.moshi.api.RetrofitService
import net.crimsonwoods.retrofit.converter.moshi.api.UserId
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URLEncoder

@RunWith(AndroidJUnit4::class)
class NestedDataTest {
    private val mockWebServer = MockWebServer()
    private val moshi = Moshi.Builder()
        .add(UserId::class.java, UserId.Adapter)
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

        service.createFavorite(name = "John Doe")

        assertEquals(1, mockWebServer.requestCount)
        val actual = mockWebServer.takeRequest()
        assertEquals("application/x-www-form-urlencoded", actual.headers["Content-Type"])
        assertEquals(
            listOf(
                """name=${"John Doe".encode()}""",
                """webSiteUrl=${"https://www.example.com".encode()}""",
                """${"place[name]".encode()}=${"test-place".encode()}""",
                """${"place[location][latitude]".encode()}=0.0""",
                """${"place[location][longitude]".encode()}=0.0""",
                """address=${"東京都".encode()}""",
                """note="""
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
