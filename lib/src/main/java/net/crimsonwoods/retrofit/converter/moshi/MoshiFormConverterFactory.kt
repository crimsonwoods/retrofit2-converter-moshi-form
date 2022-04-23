package net.crimsonwoods.retrofit.converter.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.Moshi
import okhttp3.RequestBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.http.POST
import retrofit2.http.PUT
import java.lang.reflect.Type

/**
 * Provides a Converter that converts request body from JSON to form-urlencoded format.
 *
 * Note:
 * - ___Adds [MoshiFormConverterFactory] before adding `MoshiConverterFactory`.___
 *
 * Usage:
 * ```
 * Retrofit.Builder()
 *     .addConverterFactory(MoshiFormConverterFactory.create(moshi))
 *     .addConverterFactory(MoshiConverterFactory.create(moshi))
 *     ...
 *     .build()
 * ```
 */
class MoshiFormConverterFactory private constructor(
    private val moshi: Moshi,
    private val lenient: Boolean,
    private val failOnUnknown: Boolean,
    private val serializeNulls: Boolean,
) : Converter.Factory() {
    fun asLenient(): MoshiFormConverterFactory {
        return MoshiFormConverterFactory(moshi, true, failOnUnknown, serializeNulls)
    }

    fun failOnUnknown(): MoshiFormConverterFactory {
        return MoshiFormConverterFactory(moshi, lenient, true, serializeNulls)
    }

    fun withNullSerialization(): MoshiFormConverterFactory {
        return MoshiFormConverterFactory(moshi, lenient, failOnUnknown, true)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        val methodAnnotationClasses = methodAnnotations.map { it.annotationClass }
        if (!methodAnnotationClasses.contains(POST::class) && !methodAnnotationClasses.contains(PUT::class)) {
            return null
        }

        if (!parameterAnnotations.map { it.annotationClass }.contains(Form::class)) {
            return null
        }

        var adapter: JsonAdapter<*> =
            moshi.adapter<Any?>(type, jsonAnnotations(parameterAnnotations))
        if (lenient) {
            adapter = adapter.lenient()
        }
        if (failOnUnknown) {
            adapter = adapter.failOnUnknown()
        }
        if (serializeNulls) {
            adapter = adapter.serializeNulls()
        }
        return MoshiFormRequestBodyConverter(adapter)
    }

    companion object {
        @JvmOverloads
        @JvmStatic
        fun create(moshi: Moshi = Moshi.Builder().build()): MoshiFormConverterFactory {
            return MoshiFormConverterFactory(
                moshi = moshi,
                lenient = false,
                failOnUnknown = false,
                serializeNulls = false
            )
        }

        private fun jsonAnnotations(annotations: Array<out Annotation>): Set<Annotation> {
            var result: MutableSet<Annotation>? = null
            for (annotation in annotations) {
                if (annotation.annotationClass == JsonQualifier::class) {
                    if (result == null) result = LinkedHashSet()
                    result.add(annotation)
                }
            }
            return result?.toSet() ?: emptySet()
        }
    }
}
