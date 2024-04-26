package com.example.taskmaster

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
}

