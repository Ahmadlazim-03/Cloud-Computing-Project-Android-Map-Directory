package com.mapdir.app.data.repository

import com.mapdir.app.data.model.Category
import com.mapdir.app.data.model.Meta
import com.mapdir.app.data.model.Place
import com.mapdir.app.data.remote.ApiService
import com.mapdir.app.data.remote.RetrofitClient

/**
 * Single source-of-truth for Place & Category data.
 *
 * Wraps [ApiService] calls and returns Kotlin [Result] so that
 * ViewModels never deal with raw Retrofit responses or exceptions.
 *
 * ### Integration note for Maps/GPS teammate
 * Call [getPlaces] with lat/lng to get distance-sorted results.
 * The [Place] data class contains `latitude` and `longitude` fields
 * ready for map marker placement.
 */
class PlaceRepository(
    private val api: ApiService = RetrofitClient.apiService
) {

    /**
     * Fetch all categories.
     */
    suspend fun getCategories(): Result<List<Category>> = safeApiCall {
        val response = api.getCategories()
        val body = response.body()

        if (!response.isSuccessful || body == null) {
            throw ApiException(
                body?.error?.message ?: "Gagal memuat kategori (${response.code()})"
            )
        }
        if (body.success != true) {
            throw ApiException(body.error?.message ?: "Gagal memuat kategori")
        }

        body.data ?: emptyList()
    }

    /**
     * Fetch places with optional filters.
     *
     * @return Pair of (place list, pagination meta)
     */
    suspend fun getPlaces(
        category: String? = null,
        query: String? = null,
        lat: Double? = null,
        lng: Double? = null,
        sort: String? = null,
        limit: Int? = null,
        offset: Int? = null
    ): Result<Pair<List<Place>, Meta?>> = safeApiCall {
        val response = api.getPlaces(
            category = category,
            query = query,
            lat = lat,
            lng = lng,
            sort = sort,
            limit = limit,
            offset = offset
        )
        val body = response.body()

        if (!response.isSuccessful || body == null) {
            throw ApiException(
                body?.error?.message ?: "Gagal memuat tempat (${response.code()})"
            )
        }
        if (body.success != true) {
            throw ApiException(body.error?.message ?: "Gagal memuat tempat")
        }

        val places = body.data ?: emptyList()
        Pair(places, body.meta)
    }

    /**
     * Fetch a single place by ID.
     */
    suspend fun getPlaceDetail(id: Int): Result<Place> = safeApiCall {
        val response = api.getPlaceDetail(id)
        val body = response.body()

        if (!response.isSuccessful || body == null) {
            throw ApiException(
                body?.error?.message ?: "Gagal memuat detail tempat (${response.code()})"
            )
        }
        if (body.success != true) {
            throw ApiException(body.error?.message ?: "Gagal memuat detail tempat")
        }

        body.data ?: throw ApiException("Data tempat tidak ditemukan")
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Wraps a suspend block in try-catch and returns [Result].
     * Converts common exceptions into user-friendly messages.
     */
    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Result.failure(ApiException("Tidak ada koneksi internet. Periksa jaringan Anda."))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(ApiException("Koneksi timeout. Coba lagi nanti."))
        } catch (e: java.io.IOException) {
            Result.failure(ApiException("Gangguan jaringan: ${e.localizedMessage}"))
        } catch (e: Exception) {
            Result.failure(ApiException("Terjadi kesalahan: ${e.localizedMessage}"))
        }
    }
}

/**
 * Custom exception for API-level errors (non-2xx or success=false).
 */
class ApiException(message: String) : Exception(message)
