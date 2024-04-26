package com.example.taskmaster

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import com.google.android.gms.location.LocationServices
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.Manifest
import android.content.Context

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    // ActivityResultLauncher for the location permission request
    private val locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            getLastKnownLocation() // Permission is granted, get the last known location.
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermissionAndProceed()

        // Setting up the BottomNavigationView with NavController
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.taskFragment, R.id.todayFragment, R.id.userProfileFragment -> {
                    // Show the bottom navigation view for specific destinations
                    bottomNavigationView.visibility = View.VISIBLE
                }
                else -> {
                    // Hide the bottom navigation view for other destinations
                    bottomNavigationView.visibility = View.GONE
                }
            }
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_task_list -> {
                    // Handle "Task List" click
                    navController.navigate(R.id.taskFragment)
                    true
                }
                R.id.navigation_today -> {
                    navController.navigate(R.id.todayFragment)
                    true
                }
                R.id.navigation_user -> {
                    navController.navigate(R.id.userProfileFragment)
                    true
                }
                else -> false
            }
        }

    }
    private fun checkLocationPermissionAndProceed() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can directly access the location services here.
                getLastKnownLocation()
            }
            else -> {
                // Request location permission
                locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                // Got last known location. Use this location.
                lastKnownLocation = location

                // Store the latitude and longitude in SharedPreferences
                val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putFloat(getString(R.string.saved_latitude_key), location.latitude.toFloat())
                    putFloat(getString(R.string.saved_longitude_key), location.longitude.toFloat())
                    apply() // 'apply()' writes the data to SharedPreferences asynchronously
                }

            } ?: Toast.makeText(this, "No location detected. Make sure location is enabled on the device.", Toast.LENGTH_LONG).show()
        }
    }

}

