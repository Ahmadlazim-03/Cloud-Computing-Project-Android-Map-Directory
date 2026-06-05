package com.mapdir.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrappers matching the backend contract.
 *
 * Single object  : { "success": true, "data": { ... } }
 * List + meta    : { "success": true, "data": [...], "meta": {...} }
 * Error          : { "success": false, "error": { "code": 400, "message": "..." } }
 */

// ── Single-object response ─────────────────────────────────────────────────────
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data")    val data: T?,
    @SerializedName("error")   val error: ApiErrorBody? = null
)

// ── List response with pagination metadata ─────────────────────────────────────
data class ApiListResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data")    val data: List<T>?,
    @SerializedName("meta")    val meta: Meta? = null,
    @SerializedName("error")   val error: ApiErrorBody? = null
)

// ── Pagination metadata ────────────────────────────────────────────────────────
data class Meta(
    @SerializedName("total")  val total: Int,
    @SerializedName("limit")  val limit: Int,
    @SerializedName("offset") val offset: Int
)

// ── Error body ─────────────────────────────────────────────────────────────────
data class ApiErrorBody(
    @SerializedName("code")    val code: Int,
    @SerializedName("message") val message: String
)
