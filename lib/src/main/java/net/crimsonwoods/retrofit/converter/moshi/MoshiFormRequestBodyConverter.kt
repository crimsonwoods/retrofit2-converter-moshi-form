package net.crimsonwoods.retrofit.converter.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import okhttp3.FormBody
import okhttp3.RequestBody
import okio.Buffer
import retrofit2.Converter

class MoshiFormRequestBodyConverter<T>(
    private val adapter: JsonAdapter<T>,
    private val hasArrayIndex: Boolean,
) : Converter<T, RequestBody> {
    /**
     * Converts data class to JSON, and converts JSON to form data format.
     */
    override fun convert(value: T): RequestBody? {
        val buffer = Buffer()
        val writer = JsonWriter.of(buffer)
        adapter.toJson(writer, value)
        val reader = JsonReader.of(buffer)
        return FormBody.Builder(Charsets.UTF_8)
            .with(reader)
            .build()
    }

    private fun FormBody.Builder.with(reader: JsonReader): FormBody.Builder {
        while (reader.peek() != JsonReader.Token.END_DOCUMENT) {
            val entries = when (val token = checkNotNull(reader.peek())) {
                JsonReader.Token.BEGIN_ARRAY -> {
                    reader.nextArray(ancestors = emptyList())
                }
                JsonReader.Token.BEGIN_OBJECT -> {
                    reader.nextObject(ancestors = emptyList())
                }
                JsonReader.Token.END_ARRAY,
                JsonReader.Token.END_OBJECT,
                JsonReader.Token.NAME -> {
                    throw JsonDataException("Unexpected token is detected ($token).")
                }
                JsonReader.Token.STRING -> {
                    listOf(
                        Entry(
                            name = "",
                            value = reader.nextString(),
                        )
                    )
                }
                JsonReader.Token.NUMBER -> {
                    listOf(
                        Entry(
                            name = "",
                            value = reader.nextLong().toString(),
                        )
                    )
                }
                JsonReader.Token.BOOLEAN -> {
                    listOf(
                        Entry(
                            name = "",
                            value = reader.nextBoolean().toString(),
                        )
                    )
                }
                JsonReader.Token.NULL -> {
                    listOf(
                        Entry(
                            name = "",
                            value = reader.nextNull<String>().toString(),
                        )
                    )
                }
                JsonReader.Token.END_DOCUMENT -> {
                    throw IllegalStateException("Must not be reached here.")
                }
            }
            entries.forEach { entry ->
                add(entry.name, entry.value)
            }
        }
        return this
    }

    private fun JsonReader.nextObject(ancestors: List<String>): List<Entry> {
        val entireEntries = mutableListOf<List<Entry>>()

        beginObject()

        do {
            val entries = when (checkNotNull(peek())) {
                JsonReader.Token.NAME -> {
                    val names = ancestors + nextName()
                    when (checkNotNull(peek())) {
                        JsonReader.Token.BOOLEAN -> {
                            val value = nextBoolean()
                            listOf(Entry(name = names.name(), value = value.toString()))
                        }
                        JsonReader.Token.NUMBER -> {
                            val value = nextString()
                            listOf(Entry(name = names.name(), value = value))
                        }
                        JsonReader.Token.STRING -> {
                            val value = nextString()
                            listOf(Entry(name = names.name(), value = value))
                        }
                        JsonReader.Token.NULL -> {
                            val value = nextNull<Any?>()
                            listOf(Entry(name = names.name(), value = value.toString()))
                        }
                        JsonReader.Token.BEGIN_OBJECT -> {
                            nextObject(names)
                        }
                        JsonReader.Token.BEGIN_ARRAY -> {
                            nextArray(names)
                        }
                        JsonReader.Token.END_ARRAY,
                        JsonReader.Token.END_OBJECT,
                        JsonReader.Token.NAME,
                        JsonReader.Token.END_DOCUMENT -> {
                            throw JsonDataException("Unexpected token is detected.")
                        }
                    }
                }
                JsonReader.Token.END_OBJECT -> {
                    emptyList()
                }
                JsonReader.Token.BEGIN_ARRAY,
                JsonReader.Token.END_ARRAY,
                JsonReader.Token.BEGIN_OBJECT,
                JsonReader.Token.STRING,
                JsonReader.Token.NUMBER,
                JsonReader.Token.BOOLEAN,
                JsonReader.Token.NULL,
                JsonReader.Token.END_DOCUMENT -> {
                    throw JsonDataException("Unexpected token is detected.")
                }
            }

            entireEntries.add(entries)
        } while (peek() != JsonReader.Token.END_OBJECT)

        endObject()

        return entireEntries.flatten()
    }

    private fun JsonReader.nextArray(ancestors: List<String>): List<Entry> {
        val entireEntries = mutableListOf<List<Entry>>()

        beginArray()

        var counter = 0
        do {
            val index = if (hasArrayIndex) {
                counter.toString(radix = 10)
            } else {
                ""
            }
            val name = if (ancestors.isEmpty()) {
                "[$index]"
            } else {
                index
            }
            val entries = when (checkNotNull(peek())) {
                JsonReader.Token.BEGIN_ARRAY -> {
                    nextArray(ancestors = ancestors + name)
                }
                JsonReader.Token.BEGIN_OBJECT -> {
                    nextObject(ancestors = ancestors + name)
                }
                JsonReader.Token.STRING -> {
                    listOf(
                        Entry(
                            name = (ancestors + name).name(),
                            value = nextString()
                        )
                    )
                }
                JsonReader.Token.NUMBER -> {
                    listOf(
                        Entry(
                            name = (ancestors + name).name(),
                            value = nextString()
                        )
                    )
                }
                JsonReader.Token.BOOLEAN -> {
                    listOf(
                        Entry(
                            name = (ancestors + name).name(),
                            value = nextBoolean().toString()
                        )
                    )
                }
                JsonReader.Token.NULL -> {
                    listOf(
                        Entry(
                            name = (ancestors + name).name(),
                            value = nextNull<Any>().toString()
                        )
                    )
                }
                JsonReader.Token.NAME,
                JsonReader.Token.END_OBJECT,
                JsonReader.Token.END_DOCUMENT -> {
                    throw JsonDataException("Unexpected token is detected.")
                }
                JsonReader.Token.END_ARRAY -> {
                    emptyList()
                }
            }

            entireEntries.add(entries)

            counter++
        } while (peek() != JsonReader.Token.END_ARRAY)

        endArray()

        return entireEntries.flatten()
    }

    private fun List<String>.name(): String {
        return mapIndexed { index, s ->
            if (index == 0) {
                s
            } else {
                "[$s]"
            }
        }.joinToString(separator = "")
    }

    private data class Entry(
        val name: String,
        val value: String,
    )
}
