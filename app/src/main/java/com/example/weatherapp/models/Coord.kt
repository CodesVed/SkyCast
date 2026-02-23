package com.example.weatherapp.models

data class Coord(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String? =null,
    val state: String? =null
)