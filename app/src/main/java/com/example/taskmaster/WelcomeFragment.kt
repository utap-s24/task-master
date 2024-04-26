package com.example.taskmaster

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taskmaster.R
import com.google.firebase.auth.FirebaseAuth
import com.example.taskmaster.databinding.WelcomeFragmentBinding
import io.grpc.Context
import android.content.Context.MODE_PRIVATE

class WelcomeFragment : Fragment() {

    private lateinit var binding: WelcomeFragmentBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = WelcomeFragmentBinding.inflate(inflater)
        auth = FirebaseAuth.getInstance()
        initListeners()
        return binding.root
    }

    private fun initListeners() {

        with(binding) {
            loginButton.setOnClickListener {
                signIn()
            }
            //navigate to register fragment
            signUpButton.setOnClickListener {
                findNavController().navigate(R.id.action_WelcomeFragment_to_RegisterFragment)
            }
        }
    }

    private fun signIn() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(context, "Please check related fields", Toast.LENGTH_SHORT).show()
        } else {
            binding.progressBar.isVisible = true
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    binding.progressBar.isVisible = false
                    if (task.isSuccessful) {
                        // Fetch the display name from Firebase Auth and save it to SharedPreferences
                        val sharedPreferences = requireContext().getSharedPreferences("myPreferences", MODE_PRIVATE)
                        val firebaseUsername = auth.currentUser?.displayName ?: "No username found"
                        sharedPreferences.edit().putString("username", firebaseUsername).apply()

                        Toast.makeText(context, "Authentication success.", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    } else {
                        Log.e("SignInFailed", task.exception.toString())
                        Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        binding.etEmail.text?.clear()
                        binding.etPassword.text?.clear()
                    }
                }
        }
    }



}