package com.example.weatherapp.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.weatherapp.api.ApiClient
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.models.Coord
import com.example.weatherapp.models.WeatherResponse
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.isNullOrEmpty
import androidx.core.content.edit

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

        val sharedPref = requireContext().getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        val savedCity = sharedPref.getString("lastCity", null)

        if (savedCity != null){
            val lat = sharedPref.getFloat("lastLat", 0f).toDouble()
            val lon = sharedPref.getFloat("lastLon", 0f).toDouble()

            binding.location.text = savedCity
            fetchWeather(lat, lon)
        } else {
            fetchCoordinates("Indore")
        }

        parentFragmentManager.setFragmentResultListener("cityKey", viewLifecycleOwner) {_, bundle ->
            val lat = bundle.getDouble("lat")
            val lon = bundle.getDouble("lon")
            val cityName = bundle.getString("cityName")

            Log.d("Home", "HomeFragment: Result Received! City: $cityName")

            binding.location.text = cityName
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
                        Log.e("Home", "APIFailure: ${response.code()}")
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
                    Log.d("Home", "GeoFailure: ${t.message}")
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

                        val weatherItem = weather.weather.firstOrNull()
                        val iconUrl = weatherItem?.icon?.let {
                            "https://openweathermap.org/img/wn/${it}@2x.png"
                        }

                        if (iconUrl != null){
                            Picasso.get().load(iconUrl).into(binding.weatherIcon)
                        }
                        binding.updateTime.text = "Updated: ${LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("hh:mm a"))}"
                        binding.temperature.text = "${weather.main.temp.toInt()}°C"
                        binding.condition.text = weather.weather.firstOrNull()?.description ?: "--"
                        binding.feel.text = "Feels Like: ${weather.main.feelsLike.toInt()}°C"
                        binding.humidity.text = "${weather.main.humidity}%"
                        binding.wind.text = "${weather.wind?.speed ?: 0.0} m/s"
                        binding.visibility.text = weather.visibility.let {"${it/1000.0} km"}

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

    private fun saveLastCity(name: String?, lat: Double, lon: Double){
        val sharedPref = requireContext().getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        sharedPref.edit {
            putString("lastCity", name)
            putFloat("lastLat", lat.toFloat())
            putFloat("lastLon", lon.toFloat())
        }
    }
}