package com.takturstudio.tdh8316.wedly

import org.json.JSONArray
import org.json.JSONObject
import java.io.File


fun restoreCurrentWeatherFromCache(file: File): Array<String> {
    val weather = JSONObject(file.readText())
    return arrayOf(
            JSONObject(weather.getJSONArray("weather")[0].toString()).getString("main"),
            weather.getString("name"),
            "${(weather.getJSONObject("main").getDouble("temp")).toInt()}°C")
}

fun restoreForecastsFromCache(file: File): JSONArray {
    return JSONArray(file.readText())
}
