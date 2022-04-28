package net.crimsonwoods.retrofit.converter.moshi.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Item(
    val id: Int,
    val name: String,
    val description: String,
    val elements: List<String>,
)
