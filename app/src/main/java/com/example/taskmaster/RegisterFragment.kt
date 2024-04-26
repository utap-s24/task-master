package com.example.taskmaster

import android.content.SharedPreferences
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
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import com.example.taskmaster.databinding.FragmentRegisterBinding
import android.content.Context
import com.google.firebase.auth.ktx.userProfileChangeRequest


class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater)
        auth = FirebaseAuth.getInstance()
        initListeners()
        return binding.root
    }

    private fun initListeners() {

        with(binding) {
            signUpButton.setOnClickListener {
                createNewAccount()
            }
            loginButton.setOnClickListener {
                findNavController().navigate(R.id.action_registerFragment_to_welcomeFragment)
            }
        }
    }

    private fun createNewAccount() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(context, "Please check related fields", Toast.LENGTH_SHORT).show()
        } else {
            binding.progressBar.isVisible = true
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the display name in Firebase Auth
                        val user = auth.currentUser
                        val profileUpdates = userProfileChangeRequest {
                            displayName = username
                        }
                        user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Log.d("RegisterFragment", "User profile updated.")
                            }
                        }

                        // Save username in SharedPreferences
                        val sharedPreferences = requireActivity().getSharedPreferences("myPreferences", Context.MODE_PRIVATE)
                        sharedPreferences.edit().putString("username", username).apply()

                        Toast.makeText(context, "Registration successful.", Toast.LENGTH_SHORT).show()
                        binding.progressBar.isVisible = false
                        findNavController().navigate(R.id.action_registerFragment_to_welcomeFragment)
                    } else {
                        Log.e("RegistrationFailed", task.exception.toString())
                        Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        binding.etUsername.text?.clear()
                        binding.etEmail.text?.clear()
                        binding.etPassword.text?.clear()
                    }
                    binding.progressBar.isVisible = false
                }
        }
    }

}