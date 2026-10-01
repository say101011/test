package com.example.data.search

import com.example.data.model.SearchResultPlace
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class NominatimItem(
    @Json(name = "display_name") val displayName: String?,
    @Json(name = "lat") val lat: String?,
    @Json(name = "lon") val lon: String?,
    @Json(name = "type") val type: String?
)

interface NominatimApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 8
    ): List<NominatimItem>
}

object NominatimClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"
    private val memoryCache = mutableMapOf<String, List<SearchResultPlace>>()
    private var lastRequestTime: Long = 0L

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                // Nominatim Usage Policy strictly requires a descriptive, valid User-Agent
                .header("User-Agent", "NearAlarm-AndroidApp/1.0 (Location Alarm; contact: app@nearalarm.internal)")
                .build()
            chain.proceed(request)
        }
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val api: NominatimApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NominatimApi::class.java)
    }

    suspend fun searchPlaces(query: String): Result<List<SearchResultPlace>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            return@withContext Result.success(emptyList())
        }

        val cacheKey = trimmed.lowercase()
        memoryCache[cacheKey]?.let {
            return@withContext Result.success(it)
        }

        // Throttle to respect OpenStreetMap Nominatim 1-req-per-second policy
        val now = System.currentTimeMillis()
        val elapsed = now - lastRequestTime
        if (elapsed < 1000) {
            kotlinx.coroutines.delay(1000 - elapsed)
        }
        lastRequestTime = System.currentTimeMillis()

        try {
            val response = api.search(query = trimmed)
            val results = response.mapNotNull { item ->
                val lat = item.lat?.toDoubleOrNull()
                val lon = item.lon?.toDoubleOrNull()
                val name = item.displayName
                if (lat != null && lon != null && !name.isNullOrBlank()) {
                    SearchResultPlace(
                        displayName = name,
                        latitude = lat,
                        longitude = lon,
                        type = item.type ?: ""
                    )
                } else null
            }
            memoryCache[cacheKey] = results
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
