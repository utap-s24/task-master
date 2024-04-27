package com.example.taskmaster

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taskmaster.databinding.SettingsFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest

class SettingsFragment : Fragment() {

    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        initMenu()
    }

    private fun setupUI() {
        // Retrieve username from SharedPreferences and display it in the EditText
        val sharedPreferences = requireActivity().getSharedPreferences("myPreferences", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")
        binding.editUsername.setText(username)

        // Set up the apply button to save the new username
        binding.applyButton.setOnClickListener {
            val newUsername = binding.editUsername.text.toString()
            if (newUsername.isNotEmpty()) {
                // Save the new username in SharedPreferences
                sharedPreferences.edit().putString("username", newUsername).apply()

                // Update the user's display name in Firebase Authentication
                val profileUpdates = userProfileChangeRequest {
                    displayName = newUsername
                }
                auth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Username updated successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(context, "Failed to update username", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // No settings menu in settings fragment
                menu.clear()
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Do nothing when settings is clicked
                return false
            }
        }, viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
