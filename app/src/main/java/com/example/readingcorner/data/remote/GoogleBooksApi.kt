package com.example.readingcorner.data.remote

import com.example.readingcorner.data.remote.dto.VolumeItem
import com.example.readingcorner.data.remote.dto.VolumeSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Google Books API (https://developers.google.com/books/docs/v1/using).
 * Base URL: https://www.googleapis.com/books/v1/
 *
 * Two HTTP requests used by the app:
 *  1) searchVolumes -> list of books for a query
 *  2) getVolume     -> details for a single book
 */
interface GoogleBooksApi {

    @GET("volumes")
    suspend fun searchVolumes(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 20,
        // Google Books returns HTTP 503 for volume queries unless a country is supplied.
        @Query("country") country: String = "US",
        @Query("key") apiKey: String? = null
    ): VolumeSearchResponse

    @GET("volumes/{volumeId}")
    suspend fun getVolume(
        @Path("volumeId") volumeId: String,
        @Query("country") country: String = "US",
        @Query("key") apiKey: String? = null
    ): VolumeItem
}
