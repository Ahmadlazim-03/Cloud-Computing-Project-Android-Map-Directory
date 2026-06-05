package com.mapdir.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Place DTO — maps to objects returned by GET /api/places and GET /api/places/{id}.
 *
 * Contains a nested [PlaceCategory] for the category sub-object.
 * Coordinates (latitude/longitude) are kept as Double for Maps integration.
 */
data class Place(
    @SerializedName("id")            val id: Int,
    @SerializedName("name")          val name: String,
    @SerializedName("category")      val category: PlaceCategory? = null,
    @SerializedName("address")       val address: String? = null,
    @SerializedName("latitude")      val latitude: Double? = null,
    @SerializedName("longitude")     val longitude: Double? = null,
    @SerializedName("opening_hours") val openingHours: String? = null,
    @SerializedName("description")   val description: String? = null,
    @SerializedName("rating")        val rating: Double? = null,
    @SerializedName("photo_url")     val photoUrl: String? = null,
    @SerializedName("distance_m")    val distanceM: Double? = null
)

/**
 * Nested category inside a Place object.
 * Kept separate from top-level [Category] because the shapes may diverge.
 */
data class PlaceCategory(
    @SerializedName("id")   val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String
)
