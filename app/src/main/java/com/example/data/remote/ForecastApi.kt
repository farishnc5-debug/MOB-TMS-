package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ForecastRequest(
    val series: List<Double>,
    val horizon: Int
)

@JsonClass(generateAdapter = true)
data class ForecastResponse(
    val model: String,
    val horizon: Int,
    @Json(name = "point_forecast") val pointForecast: List<Double>,
    val quantiles: Map<String, List<Double>>
)

/** TimesFM-backed demand-forecast service - see forecast-service/README.md. */
interface ForecastApi {
    @POST("v1/forecast")
    suspend fun getForecast(@Body request: ForecastRequest): ForecastResponse
}

object ForecastNetworkModule {
    // Falls back to the Android emulator's alias for the host machine's localhost so the
    // feature works against a local `uvicorn app:app` run with zero configuration.
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"

    private fun configuredBaseUrl(): String {
        val url = try {
            BuildConfig::class.java.getField("FORECAST_SERVICE_URL").get(null) as? String
        } catch (e: Exception) {
            null
        }
        return if (url.isNullOrBlank()) DEFAULT_BASE_URL else url
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS) // model inference can be slow on a cold start
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    val forecastApi: ForecastApi by lazy {
        Retrofit.Builder()
            .baseUrl(configuredBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ForecastApi::class.java)
    }
}
