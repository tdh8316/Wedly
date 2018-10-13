package com.takturstudio.tdh8316.wedly

import org.json.JSONObject
import java.io.File

@Suppress("UNUSED_PARAMETER")
fun restoreForecastsFromCache(file: File) {

}

fun restoreCurrentWeatherFromCache(file: File): Array<String> {
    val weather = JSONObject(file.readText())
    return arrayOf(
            JSONObject(weather.getJSONArray("weather")[0].toString()).getString("main"),
            weather.getString("name"),
            "${(weather.getJSONObject("main").getDouble("temp") - 273).toInt()}°C")
}
