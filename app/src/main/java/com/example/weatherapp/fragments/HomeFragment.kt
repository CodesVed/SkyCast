package com.example.weatherapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.weatherapp.ApiClient
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.models.Coord
import com.example.weatherapp.models.WeatherResponse
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.isNullOrEmpty

class HomeFragment: Fragment() {
    private val apiKey = BuildConfig.OPEN_WEATHER_API_KEY
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("API_KEY", apiKey)
        fetchCoordinates("Delhi")

    }

    private fun fetchCoordinates(city: String){
        ApiClient.api.getCityCoord(city, 1, apiKey)
            .enqueue(object: Callback<List<Coord>>{
                override fun onResponse(
                    call: Call<List<Coord>>,
                    response: Response<List<Coord>>
                ) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        val geo = response.body()!![0]
                        fetchWeather(geo.lat, geo.lon)
                    } else {
                        Log.e("Home", "APIFailure: ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: Call<List<Coord>>,
                    t: Throwable
                ) {
                    Log.d("Home", "GeoFailure: ${t.message}")
                }

            })
    }

    private fun fetchWeather(lat: Double, lon: Double){
        ApiClient.api.getWeather(lat, lon, "metric", apiKey)
            .enqueue(object : Callback<WeatherResponse>{
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful && response.body()!=null){
                        val weather = response.body()!!

                        val weatherItem = weather.weather.firstOrNull()
                        val iconUrl = weatherItem?.icon?.let {
                            "https://openweathermap.org/img/wn/${it}@2x.png"
                        }

                        binding.location.text = weather.name ?: "Unknown"
                        if (iconUrl != null){
                            Picasso.get().load(iconUrl).into(binding.weatherIcon)
                        }
                        binding.temperature.text = "${weather.main.temp.toInt()}°"
                        binding.condition.text = weather.weather.firstOrNull()?.description ?: "--"
                        binding.feel.text = "Feels Like: ${weather.main.feelsLike.toInt()}°"
                        binding.humidity.text = "${weather.main.humidity}%"
                        binding.wind.text = "${weather.wind?.speed ?: 0.0} m/s"
                        binding.visibility.text = weather.visibility?.let {"${it/1000} km"} ?: "N/A"
                    } else {
                        Log.e("Home", "APIFailure: ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: Call<WeatherResponse>,
                    t: Throwable
                ) {
                    Log.d("Home", "WeatherFailure: ${t.message}")
                }

            })
    }
}