package net.crimsonwoods.retrofit.converter.moshi.api

import com.squareup.moshi.Moshi
import net.crimsonwoods.retrofit.converter.moshi.Form
import net.crimsonwoods.retrofit.converter.moshi.MoshiFormConverterFactory
import okhttp3.HttpUrl
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

class RetrofitService(
    url: HttpUrl,
    moshi: Moshi,
) {
    interface Service {
        @POST("/user")
        suspend fun createNewUser(@Body @Form request: CreateNewUserRequest)

        @PUT("/user")
        suspend fun updateUser(@Body @Form request: UpdateUserRequest)

        @POST("/favorite")
        suspend fun createFavorite(@Body @Form request: Favorite)
    }

    private val service = Retrofit.Builder()
        .addConverterFactory(MoshiFormConverterFactory.create(moshi))
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(url)
        .build()
        .create(Service::class.java)

    suspend fun createNewUser(name: String, age: Int?) {
        service.createNewUser(CreateNewUserRequest(name = name, age = age))
    }

    suspend fun updateUser(id: UserId, name: String, age: Int?, rank: UserRank) {
        service.updateUser(
            UpdateUserRequest(
                id = id,
                name = name,
                age = age,
                rank = rank,
            )
        )
    }

    suspend fun createFavorite(name: String) {
        service.createFavorite(
            Favorite(
                name = name,
                webSiteUrl = "https://www.example.com",
                place = Favorite.Place(
                    name = "test-place",
                    location = Favorite.Location(0.0, 0.0),
                ),
                address = "東京都",
                note = "",
            )
        )
    }
}
