package net.crimsonwoods.retrofit.converter.moshi.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Favorite(
    val name: String,
    val webSiteUrl: String,
    val place: Place?,
    val address: String,
    val note: String,
) {
    @JsonClass(generateAdapter = true)
    data class Place(
        val name: String,
        val location: Location,
    )

    @JsonClass(generateAdapter = true)
    data class Location(
        val latitude: Double,
        val longitude: Double,
    )
}
