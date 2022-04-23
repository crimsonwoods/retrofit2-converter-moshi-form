package net.crimsonwoods.retrofit.converter.moshi.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateUserRequest(
    val id: UserId,
    val name: String,
    val age: Int?,
    val rank: UserRank,
)
