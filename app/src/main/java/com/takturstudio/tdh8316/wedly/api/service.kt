package com.takturstudio.tdh8316.wedly.api

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

val WEATHER_TO_KOREAN: HashMap<String, String> = hashMapOf(
        "Clear" to "맑음",
        "Drizzle" to "보슬비",
        "Thunderstorm" to "뇌우",
        "Rain" to "비",
        "Snow" to "눈",
        "Clouds" to "구름",
        "Fog" to "안개"
)

interface WeatherAPI {
    @GET("weather?")
    fun getCurrentWeather(
            @Query("lat") lat: String,
            @Query("lon") lon: String,
            @Query("APPID") APPID: String,
            @Query("units") units: String = "metric")
            : Call<JsonObject>

    @GET("weather?")
    fun getCurrentWeatherWithoutGPS(
            @Query("q") lat: String,
            @Query("APPID") APPID: String,
            @Query("units") units: String = "metric")
            : Call<JsonObject>

    @GET("forecast?")
    fun getForecasts(
            @Query("lat") lat: String,
            @Query("lon") lon: String,
            @Query("APPID") APPID: String,
            @Query("units") units: String = "metric")
            : Call<JsonObject>
}
