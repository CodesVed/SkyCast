package com.example.weatherapp.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.api.ApiClient
import com.example.weatherapp.databinding.FragmentWeatherBinding
import com.example.weatherapp.models.Coord
import com.example.weatherapp.models.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class WeatherFragment: Fragment() {

    private val apiKey = BuildConfig.OPEN_WEATHER_API_KEY
    private lateinit var binding: FragmentWeatherBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireContext().getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        val savedCity = sharedPref.getString("lastCity", null)

        if (savedCity != null) {
            val lat = sharedPref.getFloat("lastLat", 0f).toDouble()
            val lon = sharedPref.getFloat("lastLon", 0f).toDouble()

            fetchWeather(lat, lon)
        } else {
            fetchCoordinates("Indore")
        }

        parentFragmentManager.setFragmentResultListener(
            "cityKey",
            viewLifecycleOwner
        ) { _, bundle ->
            val lat = bundle.getDouble("lat")
            val lon = bundle.getDouble("lon")
            val cityName = bundle.getString("cityName")

            fetchWeather(lat, lon)
            saveLastCity(cityName, lat, lon)
        }
    }


    private fun fetchCoordinates(city: String){
        ApiClient.api.getCityCoord(city = city, apiKey = apiKey)
            .enqueue(object: Callback<List<Coord>>{
                override fun onResponse(
                    call: Call<List<Coord>>,
                    response: Response<List<Coord>>
                ) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        val geo = response.body()!![0]
                        fetchWeather(geo.lat, geo.lon)
                    } else {
                        Log.e("Weather", "APIFailure: ${response.code()}")
                        Toast.makeText(requireContext(), "City not found", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<Coord>>,
                    t: Throwable
                ) {
                    val errorMessage = when(t){
                        is UnknownHostException -> "No internet connection"
                        is SocketTimeoutException -> "Connection timeout"
                        else -> "Something went wrong"
                    }
                    Log.d("Weather", "GeoFailure: ${t.message}")
                }

            })
    }
    private fun fetchWeather(lat: Double, lon: Double){
        ApiClient.api.getWeather(lat = lat, lon = lon, apiKey = apiKey, units = "metric")
            .enqueue(object : Callback<WeatherResponse>{
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful && response.body()!=null){
                        val weather = response.body()!!

                        val durationRise = dateTime(weather.sys.sunrise, "Asia/Kolkata")
                        val durationSet = dateTime(weather.sys.sunset, "Asia/Kolkata")

                        binding.pressure.text = "${weather.main.pressure} hPa"
                        binding.minTemp.text = "${weather.main.tempMin.toInt()}°C"
                        binding.maxTemp.text = "${weather.main.tempMax.toInt()}°C"
                        binding.sunrise.text = "${durationRise}"
                        binding.sunset.text = "${durationSet}"

                    } else {
                        Log.e("Weather", "APIFailure: ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: Call<WeatherResponse>,
                    t: Throwable
                ) {
                    Log.d("Weather", "WeatherFailure: ${t.message}")
                }

            })
    }
    private fun saveLastCity(name: String?, lat: Double, lon: Double){
        val sharedPref = requireContext().getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        sharedPref.edit {
            putString("lastCity", name)
            putFloat("lastLat", lat.toFloat())
            putFloat("lastLon", lon.toFloat())
        }
    }

    // Source - https://stackoverflow.com/a/68164330
// Posted by deHaar, modified by community. See post 'Timeline' for change history
// Retrieved 2026-02-25, License - CC BY-SA 4.0

    private fun dateTime(time: Int, zone: String, format: String = "hh:mm a"): String {
        val zoneId = ZoneId.of(zone)
        val instant = Instant.ofEpochSecond(time.toLong())
        val formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH)
        return instant.atZone(zoneId).format(formatter)
    }
}