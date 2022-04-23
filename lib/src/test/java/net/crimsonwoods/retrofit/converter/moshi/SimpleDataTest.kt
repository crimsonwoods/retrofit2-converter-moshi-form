package net.crimsonwoods.retrofit.converter.moshi

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squareup.moshi.Moshi
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import net.crimsonwoods.retrofit.converter.moshi.api.RetrofitService
import net.crimsonwoods.retrofit.converter.moshi.api.UserId
import net.crimsonwoods.retrofit.converter.moshi.api.UserRank
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleDataTest {
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
    fun simpleFlatData() = runBlocking {
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(200)
            setBody("""{}""")
        })

        service.createNewUser(name = "John Doe", 30)

        assertEquals(1, mockWebServer.requestCount)
        val actual = mockWebServer.takeRequest()
        assertEquals("application/x-www-form-urlencoded", actual.headers["Content-Type"])
        assertEquals("name=John%20Doe&age=30", actual.body.readUtf8())
    }

    @Test
    fun customAdapter() = runBlocking {
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(200)
            setBody("""{}""")
        })

        service.updateUser(id = UserId(1), name = "John Doe", age = 31, rank = UserRank.JUNIOR)

        assertEquals(1, mockWebServer.requestCount)
        val actual = mockWebServer.takeRequest()
        assertEquals("application/x-www-form-urlencoded", actual.headers["Content-Type"])
        assertEquals("id=1&name=John%20Doe&age=31&rank=JUNIOR", actual.body.readUtf8())
    }
}
