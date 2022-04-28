package net.crimsonwoods.retrofit.converter.moshi.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddItemsRequest(
    val items: List<Item>,
)
