package net.crimsonwoods.retrofit.converter.moshi

/**
 * Parameter annotation for Retrofit2 used instead of `@FormUrlEncoded` annotation.
 *
 * Usage:
 * ```
 * interface Service {
 *     // `Parameters` are converted to form body (application/x-www-form-urlencoded)
 *     @POST("/your_api")
 *     suspend fun yourApi(@Body @Form request: Parameters)
 * }
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Form
