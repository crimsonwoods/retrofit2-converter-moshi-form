package net.crimsonwoods.retrofit.converter.moshi.api

import com.squareup.moshi.*

data class ItemId(
    private val rawValue: Int,
) {
    object Adapter : JsonAdapter<ItemId>() {
        @FromJson
        override fun fromJson(reader: JsonReader): ItemId {
            return ItemId(reader.nextInt())
        }

        @ToJson
        override fun toJson(writer: JsonWriter, value: ItemId?) {
            if (value == null) {
                writer.nullValue()
            } else {
                writer.value(value.rawValue)
            }
        }
    }
}
