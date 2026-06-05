package com.mapdir.app.data.remote

import com.mapdir.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client.
 *
 * ### Cara mengganti Base URL
 * Buka `app/build.gradle.kts`, cari `buildConfigField("String", "BASE_URL", ...)`
 * lalu ganti nilainya. Bisa juga override per build type (debug / release).
 *
 * Contoh:
 * ```
 * buildConfigField("String", "BASE_URL", "\"https://api.production.com/api\"")
 * ```
 *
 * Pastikan URL diakhiri dengan `/` agar Retrofit resolve path dengan benar.
 */
object RetrofitClient {

    // Append trailing slash if missing — Retrofit requirement
    private val baseUrl: String
        get() {
            val url = BuildConfig.BASE_URL
            return if (url.endsWith("/")) url else "$url/"
        }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
