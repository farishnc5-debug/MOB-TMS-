package com.example.tracker.net

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/** Identifies this app to the free public OSM services, per Nominatim's usage policy. */
private val userAgentInterceptor = Interceptor { chain ->
    val request = chain.request().newBuilder()
        .header("User-Agent", "YallaMuvTracker/1.0 (+https://github.com/)")
        .build()
    chain.proceed(request)
}

private val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .addInterceptor(userAgentInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
}

private val moshi: Moshi by lazy {
    Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
}

private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

object NetworkModule {
    val osrmApi: OsrmApi by lazy {
        retrofit("https://router.project-osrm.org/").create(OsrmApi::class.java)
    }
    val nominatimApi: NominatimApi by lazy {
        retrofit("https://nominatim.openstreetmap.org/").create(NominatimApi::class.java)
    }
}
