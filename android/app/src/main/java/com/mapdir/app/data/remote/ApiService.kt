package com.mapdir.app.data.remote

import com.mapdir.app.data.model.ApiListResponse
import com.mapdir.app.data.model.ApiResponse
import com.mapdir.app.data.model.Category
import com.mapdir.app.data.model.Place
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface defining all API endpoints.
 *
 * Base URL is configured in [RetrofitClient] via BuildConfig.BASE_URL.
 */
interface ApiService {

    // ── Categories ─────────────────────────────────────────────────────────────
    @GET("categories")
    suspend fun getCategories(): Response<ApiListResponse<Category>>

    // ── Places (list with filters) ─────────────────────────────────────────────
    @GET("places")
    suspend fun getPlaces(
        @Query("category") category: String? = null,
        @Query("q")        query: String? = null,
        @Query("lat")      lat: Double? = null,
        @Query("lng")      lng: Double? = null,
        @Query("sort")     sort: String? = null,
        @Query("limit")    limit: Int? = null,
        @Query("offset")   offset: Int? = null
    ): Response<ApiListResponse<Place>>

    // ── Place detail ───────────────────────────────────────────────────────────
    @GET("places/{id}")
    suspend fun getPlaceDetail(
        @Path("id") id: Int
    ): Response<ApiResponse<Place>>
}
