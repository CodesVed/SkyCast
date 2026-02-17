package com.example.weatherapp

import com.example.weatherapp.models.Coord
import com.example.weatherapp.models.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APIInterface {

    @GET("geo/1.0/direct")
    fun getCityCoord(
        @Query("q") city: String,
        @Query("limit") limit: Int=1,
        @Query("appid") apiKey: String
    ): Call<List<Coord>>

    @GET("data/2.5/weather")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String ="metric",
        @Query("appid") apiKey: String
    ): Call<WeatherResponse>
}