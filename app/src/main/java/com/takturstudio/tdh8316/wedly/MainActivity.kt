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
import com.takturstudio.tdh8316.wedly.api.OPEN_WEATHER_MAP_KEY
import com.takturstudio.tdh8316.wedly.api.RetrofitClient
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        image_weather.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotation))

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }

        startService()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 0) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder(this).setTitle("PermissionError").setMessage("Access is denied.").setPositiveButton("OK")
                { _, _ -> exitProcess(0) }.show()
            }
        }
    }

    private fun startService() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
        }
    }

    private val locationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            // 위도, 경도 = location.latitude, location.longitude
            updateWeather(location.latitude, location.longitude)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
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

                    @SuppressLint("SetTextI18n")
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        val weather = JSONObject(response.body().toString())
                        label_city.text = weather.getString("name")
                        label_weather.text = JSONObject(weather.getJSONArray("weather")[0].toString()).getString("main")
                        label_temperature.text = "${(weather.getJSONObject("main").getDouble("temp") - 273.15).toInt()}°C"

                        when (label_weather.text) {
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
                })
        image_weather.clearAnimation()
    }
}
