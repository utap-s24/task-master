package com.example.taskmaster.user

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taskmaster.R
import com.example.taskmaster.databinding.PfpPageBinding
import com.google.firebase.auth.FirebaseAuth

class UserProfileFragment : Fragment() {

    private var _binding: PfpPageBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PfpPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        // Retrieve username from SharedPreferences and display it
        val sharedPreferences = requireActivity().getSharedPreferences("myPreferences", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "No username found")

        // Log the retrieved username
        Log.d("UserProfileFragment", "Retrieved username: $username")

        // Update the TextView with the retrieved username
        if (username != null && username != "No username found") {
            binding.txtUsername.text = getString(R.string.username_format, username)
        } else {
            binding.txtUsername.text = username
        }

        //settings button
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_userProfileFragment_to_SettingsFragment)
        }
        //logout button
        binding.btnLogout.setOnClickListener {
            auth.signOut() // Sign out from Firebase Auth
            // Do not remove the username from SharedPreferences if you want it to persist after logout
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_userProfileFragment_to_WelcomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
