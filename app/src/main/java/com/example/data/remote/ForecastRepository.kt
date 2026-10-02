package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ForecastRepository(
    private val api: ForecastApi = ForecastNetworkModule.forecastApi
) {
    suspend fun forecastDailyVolume(series: List<Double>, horizon: Int): Result<ForecastResponse> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.getForecast(ForecastRequest(series = series, horizon = horizon)))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
