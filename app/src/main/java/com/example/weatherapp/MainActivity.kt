package com.example.weatherapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.fragments.HomeFragment
import com.example.weatherapp.fragments.SearchFragment
import com.example.weatherapp.fragments.WeatherFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        replaceFragment(HomeFragment())
        binding.bottomNavBar.selectedItemId = R.id.item2

        binding.bottomNavBar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.item1 ->replaceFragment(SearchFragment())
                R.id.item2 ->replaceFragment(HomeFragment())
                R.id.item3 ->replaceFragment(WeatherFragment())
            }
            true
        }

    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout,fragment)
//            .addToBackStack(null)
            .commit()
    }
}