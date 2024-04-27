package com.example.taskmaster.user

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.taskmaster.R
import com.example.taskmaster.data.FirestoreRepositoryImpl
import com.example.taskmaster.data.RequestState
import com.example.taskmaster.databinding.PfpPageBinding
import com.example.taskmaster.tasklist.HomeViewModel
import com.example.taskmaster.tasklist.HomeViewModelFactory
import com.example.taskmaster.tasklist.UserProfileViewModel
import com.example.taskmaster.tasklist.UserProfileViewModelFactory
import com.example.taskmaster.usecase.CreateNoteUseCase
import com.example.taskmaster.usecase.DeleteNoteUseCase
import com.example.taskmaster.usecase.GetCategoryCount
import com.example.taskmaster.usecase.GetFilterNotesUseCase
import com.example.taskmaster.usecase.GetNotesUseCase
import com.example.taskmaster.usecase.UpdateNoteUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class UserProfileFragment : Fragment() {

    private var _binding: PfpPageBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }

    // Use the 'auth' instance to instantiate FirestoreRepositoryImpl
    private val firestoreRepositoryImpl = FirestoreRepositoryImpl(FirebaseFirestore.getInstance(), auth)

    // Now use the firestoreRepositoryImpl to create your use cases
    private val getCount = GetCategoryCount(firestoreRepositoryImpl)

    // Create the ViewModel factory with your use cases
    private val factory = UserProfileViewModelFactory(getCount)

    // Use the factory to create the ViewModel
    private val userProfileViewModel by lazy { ViewModelProvider(this, factory).get(UserProfileViewModel::class.java) }


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
//        getNotes()
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

    private fun getNotes() {
        userProfileViewModel.getCount()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userProfileViewModel.countState.collect { countState ->
                    // Handle the emitted value
                    if (countState != null) {
                        when (countState) {
                            is RequestState.Loading -> {
                                // Show loading indicator (optional)
                            }
                            is RequestState.Success -> {
                                val count = countState.data // Access the count data from the Success state
                                // Update UI or perform actions with the retrieved count (e.g., ArrayList<Int>)
                                Log.d("Notes", "Number of notes: ${count}")
                            }
                            is RequestState.Error -> {
                                // Handle errors (optional)
                                val exception = countState.exception
                                Log.e("Notes", "Error getting count:", exception)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
