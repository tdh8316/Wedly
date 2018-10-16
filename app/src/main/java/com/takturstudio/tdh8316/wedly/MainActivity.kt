package com.takturstudio.tdh8316.wedly

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import android.view.animation.AnimationUtils
import com.google.gson.JsonObject
import com.takturstudio.tdh8316.wedly.api.MIN_CALL_TIME
import com.takturstudio.tdh8316.wedly.api.OPEN_WEATHER_MAP_KEY
import com.takturstudio.tdh8316.wedly.api.RetrofitClient
import com.takturstudio.tdh8316.wedly.api.WEATHER_TO_KOREAN
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private var forecastsCache: File? = null
    private var currentsCache: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        image_weather.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotation))
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        forecastsCache = File("${getExternalFilesDir(null)}",
                "forecasts.json")
        currentsCache = File("${getExternalFilesDir(null)}",
                "latest.json")

        if (PackageManager.PERMISSION_DENIED in listOf(
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION),
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE),
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        } else startService()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 0) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder(this).setTitle("PermissionError").setMessage("Access is denied.").setPositiveButton("OK")
                { _, _ -> exitProcess(0) }.show()
            } else startService()
        }
    }

    private fun startService() {
        if (forecastsCache!!.exists() && currentsCache!!.exists()) {
            restoreForecastsFromCache(forecastsCache!!)

            val latestWeather = restoreCurrentWeatherFromCache(currentsCache!!)
            updateCurrentWeatherWidgets(latestWeather[0], latestWeather[1], latestWeather[2])
            updateForecastsWidgets(restoreForecastsFromCache(forecastsCache!!))

            image_weather.clearAnimation()

            Log.d("Restore", "Done")
        }

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, MIN_CALL_TIME, 0f, locationListener)
        }
    }

    private val locationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            // 위도, 경도 = location.latitude, location.longitude
            updateWeather(location.latitude, location.longitude)
            updateForecasts(location.latitude, location.longitude)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun updateForecasts(latitude: Double, longitude: Double) {
        RetrofitClient.getInstance().buildRetrofit()
                .getForecasts(
                        latitude.toString(),
                        longitude.toString(),
                        OPEN_WEATHER_MAP_KEY
                ).enqueue(object : Callback<JsonObject> {
                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Log.e("openweathermap", t.message)
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        val forecasts = JSONObject(response.body().toString()).getJSONArray("list")
                        forecastsCache!!.createNewFile()
                        forecastsCache!!.writeText(forecasts.toString())

                        updateForecastsWidgets(forecasts)
                    }
                })
    }

    private fun updateWeather(latitude: Double, longitude: Double) {
        RetrofitClient.getInstance().buildRetrofit()
                .getCurrentWeather(
                        latitude.toString(),
                        longitude.toString(),
                        OPEN_WEATHER_MAP_KEY
                ).enqueue(object : Callback<JsonObject> {
                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Log.e("openweathermap", t.message)
                    }

                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        val weather = JSONObject(response.body().toString())
                        currentsCache!!.createNewFile()
                        currentsCache!!.writeText(weather.toString())

                        updateCurrentWeatherWidgets(
                                JSONObject(weather.getJSONArray("weather")[0].toString()).getString("main"),
                                weather.getString("name"),
                                "${(weather.getJSONObject("main").getDouble("temp")).toInt()}℃")
                        image_weather.clearAnimation()
                    }
                })
    }

    fun updateCurrentWeatherWidgets(weather: String, city: String, temperature: String) {
        label_weather.text = WEATHER_TO_KOREAN[weather]
        label_temperature.text = temperature
        label_city.text = city

        when (weather) {
            "Clear" -> {
                val timeNow = GregorianCalendar(TimeZone.getTimeZone("Asia/Seoul")).get(GregorianCalendar.HOUR_OF_DAY)
                when {
                    20 <= timeNow -> {
                        layout.background = getDrawable(R.drawable.background_night)
                        image_weather.setImageDrawable(getDrawable(R.drawable.moon))
                    }
                    18 <= timeNow -> {
                        layout.background = getDrawable(R.drawable.background_sunset)
                        image_weather.setImageDrawable(getDrawable(R.drawable.moon))
                    }
                    else -> {
                        layout.background = getDrawable(R.drawable.background_blue)
                        image_weather.setImageDrawable(getDrawable(R.drawable.sun))
                    }
                }
            }
            "Drizzle" -> {
                layout.background = getDrawable(R.drawable.background_gray)
                image_weather.setImageDrawable(getDrawable(R.drawable.sun_with_clouds))
            }
            "Clouds" -> {
                val timeNow = GregorianCalendar(TimeZone.getTimeZone("Asia/Seoul")).get(GregorianCalendar.HOUR_OF_DAY)
                if (18 <= timeNow) {
                    layout.background = getDrawable(R.drawable.background_nightfog)
                } else {
                    layout.background = getDrawable(R.drawable.background_gray)
                }
                image_weather.setImageDrawable(getDrawable(R.drawable.clouds))
            }
            "Snow" -> {
                layout.background = getDrawable(R.drawable.background_snow)
                image_weather.setImageDrawable(getDrawable(R.drawable.clouds_with_snow))
            }
            "Rain" -> {
                layout.background = getDrawable(R.drawable.background_gray)
                image_weather.setImageDrawable(getDrawable(R.drawable.clouds_with_rain))
            }
            "Fog" -> {
                layout.background = getDrawable(R.drawable.background_gray)
                image_weather.setImageDrawable(getDrawable(R.drawable.clouds_with_rain))
            }
            else -> {

            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateForecastsWidgets(forecasts: JSONArray) {
        for (i in 3..9) {
            // Log.d("forecast", "Temp=${JSONObject(forecasts[i].toString()).getJSONObject("main").getDouble("temp")}")
            /*Log.d("time", JSONObject(forecasts[i].toString()).getString("dt_txt").substring(11, 13))
            TimeUnit.SECONDS.sleep(1L)
            Log.d("temp", (JSONObject(forecasts[i].toString()).getJSONObject("main").getDouble("temp")-273).toInt().toString())*/
            val temp = (JSONObject(forecasts[i].toString()).getJSONObject("main").getDouble("temp")).toInt()
            val time = if (JSONObject(forecasts[i].toString()).getString("dt_txt").substring(11, 13).startsWith("0")) {
                "오전 ${JSONObject(forecasts[i].toString()).getString("dt_txt").substring(12, 13)}시"
            } else {
                "오후 ${JSONObject(forecasts[i].toString()).getString("dt_txt").substring(11, 13).toInt() - 12}시".replace("오후 0시", "오후 12시")
            }
            when ((i - 3)) {
                0 -> {
                    label_temperature_fc0.text = "${temp}℃"
                    label_time_fc0.text = time
                }
                1 -> {
                    label_temperature_fc1.text = "${temp}℃"
                    label_time_fc1.text = time
                }
                2 -> {
                    label_temperature_fc2.text = "${temp}℃"
                    label_time_fc2.text = time
                }
                3 -> {
                    label_temperature_fc3.text = "${temp}℃"
                    label_time_fc3.text = time
                }
                4 -> {
                    label_temperature_fc4.text = "${temp}℃"
                    label_time_fc4.text = time
                }
                5 -> {
                    label_temperature_fc5.text = "${temp}℃"
                    label_time_fc5.text = time
                }
            }
        }
    }
}
