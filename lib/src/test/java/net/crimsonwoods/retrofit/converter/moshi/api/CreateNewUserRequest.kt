package net.crimsonwoods.retrofit.converter.moshi.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateNewUserRequest(
    val name: String,
    val age: Int?,
)
