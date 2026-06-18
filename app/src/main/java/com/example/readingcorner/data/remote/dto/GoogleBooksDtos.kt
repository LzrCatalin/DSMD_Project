package com.example.readingcorner.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Top-level response for GET /volumes?q=... */
data class VolumeSearchResponse(
    @SerializedName("totalItems") val totalItems: Int = 0,
    @SerializedName("items") val items: List<VolumeItem>? = null
)

/** A single book ("volume") returned by the API. */
data class VolumeItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("volumeInfo") val volumeInfo: VolumeInfo? = null
)

data class VolumeInfo(
    @SerializedName("title") val title: String? = null,
    @SerializedName("authors") val authors: List<String>? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("pageCount") val pageCount: Int? = null,
    @SerializedName("categories") val categories: List<String>? = null,
    @SerializedName("averageRating") val averageRating: Double? = null,
    @SerializedName("imageLinks") val imageLinks: ImageLinks? = null
)

data class ImageLinks(
    @SerializedName("smallThumbnail") val smallThumbnail: String? = null,
    @SerializedName("thumbnail") val thumbnail: String? = null
)
