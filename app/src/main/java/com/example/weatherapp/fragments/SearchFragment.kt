package com.example.weatherapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import com.example.weatherapp.R
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.api.ApiClient
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.adapters.MyAdapter
import com.example.weatherapp.databinding.FragmentSearchBinding
import com.example.weatherapp.models.Coord
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SearchFragment: Fragment() {

    private val apiKey = BuildConfig.OPEN_WEATHER_API_KEY
    private lateinit var binding: FragmentSearchBinding
    private lateinit var cityRecyclerview: RecyclerView
    private lateinit var adapter: MyAdapter
    private lateinit var cityList: ArrayList<Coord>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cityList = arrayListOf()
        cityRecyclerview = binding.cityRecyclerview
        adapter = MyAdapter(requireActivity(), cityList)
        cityRecyclerview.adapter = adapter

        adapter.setOnClickListener(object : MyAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                if (position < cityList.size){
                    val selectedCity = cityList[position]
                    Log.d("Search", "SearchFragment: Item Clicked at position $position. Popping backstack.")

                    val result = Bundle().apply {
                        putDouble("lat", selectedCity.lat)
                        putDouble("lon", selectedCity.lon)
                        putString("cityName", selectedCity.name)
                    }
                    parentFragmentManager.setFragmentResult("cityKey", result)

                    val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavBar)
                    bottomNav.selectedItemId = R.id.item2
                }
            }
        })

        fetchCitySuggestions("Indore")

        binding.citySearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isNotEmpty()){
                    fetchCitySuggestions(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()){
                    fetchCitySuggestions("India")
                } else if (newText.length > 2) {
                    fetchCitySuggestions(newText)
                }
                return true
            }

        })
    }

    private fun fetchCitySuggestions(city: String){
        ApiClient.api.getCityCoord(city = city, apiKey = apiKey, limit = 10)
            .enqueue(object: Callback<List<Coord>>{
                override fun onResponse(
                    call: Call<List<Coord>>,
                    response: Response<List<Coord>>
                ) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        val geo = response.body()!!
                        adapter.updateData(geo)
                    } else {
                        Log.e("Search", "APIFailure: ${response.code()}")
                        adapter.updateData(emptyList())
                        Toast.makeText(requireContext(), "City not found", Toast.LENGTH_LONG).show()
                    }
                    Log.d("Search", "Cities found: ${response.body()?.size}")
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
                    Log.d("Search", "GeoFailure: ${t.message}")
                }

            })
    }
}