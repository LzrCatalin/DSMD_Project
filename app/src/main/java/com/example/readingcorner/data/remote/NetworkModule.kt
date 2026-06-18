package com.example.readingcorner.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Lightweight manual DI for the Google Books API.
 *
 * API KEY: basic search works without a key (low quota). To use a key, paste it
 * into [BooksApiConfig.API_KEY]. When blank, the key query param is simply omitted.
 */
object BooksApiConfig {
    // TODO: paste your Google Books API key here (optional). Leave "" to call without a key.
    const val API_KEY: String = "AIzaSyCk_wopjgZF9vRfW_lr84IsocXHNWmAdqs"

    val keyOrNull: String?
        get() = API_KEY.ifBlank { null }
}

object NetworkModule {

    private const val BASE_URL = "https://www.googleapis.com/books/v1/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    val googleBooksApi: GoogleBooksApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleBooksApi::class.java)
    }
}
