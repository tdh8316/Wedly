package com.takturstudio.tdh8316.wedly.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val OPEN_WEATHER_MAP_KEY = "71594a4e523a6036a46aa2f1bd9f345d"
const val MIN_CALL_TIME: Long = 300000 // 300000ms=5m 3600000ms=1h

class RetrofitClient {

    companion object {
        private val retrofitClient: RetrofitClient = RetrofitClient()

        fun getInstance(): RetrofitClient {
            return retrofitClient
        }
    }

    fun buildRetrofit(): WeatherAPI {
        val retrofit: Retrofit? = Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        return retrofit!!.create(WeatherAPI::class.java)
    }

}