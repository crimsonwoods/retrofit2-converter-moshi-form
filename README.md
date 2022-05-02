Converter for form request
=====

This library provides a Converter used for Retrofit2[^1].

Converter uses Moshi[^2] for serialization to form-urlencoded type request body.

## Setup

## Usage

Adds an instance of `MoshiFormConverterFactory` to `Retrofit.Builder` parameter like below:

```Kotlin
val service = Retrofit.Builder()
    .addConverterFactory(MoshiFormConverterFactory.create(moshi)) // here
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(yourBaseUrl)
    .build()
    .create(YourService::class.java)
```

And annotates a request parameter with `@Form` annotation like below:

```Kotlin
interface YourService {
    @POST("/your_api")
    suspend fun yourApi(@Body @Form request: ComplexRequest)
}
```

## License

MIT License



[^1]: https://github.com/square/retrofit
[^2]: https://github.com/square/moshi