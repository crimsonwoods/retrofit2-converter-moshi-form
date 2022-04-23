package net.crimsonwoods.retrofit.converter.moshi.api

import com.squareup.moshi.*

data class UserId(
    private val rawValue: Int,
) {
    object Adapter : JsonAdapter<UserId>() {
        @FromJson
        override fun fromJson(reader: JsonReader): UserId {
            return UserId(reader.nextInt())
        }

        @ToJson
        override fun toJson(writer: JsonWriter, value: UserId?) {
            if (value == null) {
                writer.nullValue()
            } else {
                writer.value(value.rawValue)
            }
        }
    }
}
