package com.mapdir.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Category DTO — maps to GET /api/categories response items.
 *
 * JSON: { "id": 1, "name": "Restoran", "slug": "restoran", "icon": "🍽️" }
 */
data class Category(
    @SerializedName("id")   val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("icon") val icon: String? = null
)
