package com.example.taskmaster.tasklist

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.OnClickListener
import com.example.taskmaster.R
import com.example.taskmaster.SharedPreferences
import com.example.taskmaster.SwipeGesture
import com.example.taskmaster.data.FirestoreRepository
import com.example.taskmaster.data.FirestoreRepositoryImpl
import com.example.taskmaster.data.Note
import com.example.taskmaster.data.RequestState
import com.example.taskmaster.databinding.TaskFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.example.taskmaster.usecase.CreateNoteUseCase
import com.example.taskmaster.usecase.DeleteNoteUseCase
import com.example.taskmaster.usecase.GetNotesUseCase
import com.example.taskmaster.usecase.UpdateNoteUseCase
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var binding: TaskFragmentBinding
    private lateinit var notesList: ArrayList<Note>

    // Use a single instance of FirebaseAuth
    private val auth by lazy { FirebaseAuth.getInstance() }

    // Use the 'auth' instance to instantiate FirestoreRepositoryImpl
    private val firestoreRepositoryImpl = FirestoreRepositoryImpl(FirebaseFirestore.getInstance(), auth)

    // Now use the firestoreRepositoryImpl to create your use cases
    private val createNoteUseCase = CreateNoteUseCase(firestoreRepositoryImpl)
    private val getNotesUseCase = GetNotesUseCase(firestoreRepositoryImpl)
    private val updateNoteUseCase = UpdateNoteUseCase(firestoreRepositoryImpl)
    private val deleteNoteUseCase = DeleteNoteUseCase(firestoreRepositoryImpl)

    // Create the ViewModel factory with your use cases
    private val factory = HomeViewModelFactory(createNoteUseCase, getNotesUseCase, updateNoteUseCase, deleteNoteUseCase)

    // Use the factory to create the ViewModel
    private val homeViewModel by lazy { ViewModelProvider(this, factory).get(HomeViewModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TaskFragmentBinding.inflate(inflater)

        binding.tvWelcome.text =
            "Welcome ${SharedPreferences(requireContext()).getUsernameString()}!"
        initListeners()
        getNotes()
        onBackPressed()

        return binding.root
    }

    private fun initListeners() {
        with(binding) {
            ivLogout.setOnClickListener {
                initLogoutDialog()
            }
            btnCreateItem.setOnClickListener {
                showCreateDialog()
            }
            swipeRefreshLayout.setOnRefreshListener {
                getNotes()
            }
        }
    }

    private fun initLogoutDialog() {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setTitle("Logout")
        alertDialogBuilder.setMessage("Are you sure you want to exit?")
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            auth.signOut() // Use the single 'auth' instance
            findNavController().navigate(R.id.action_WelcomeFragment_to_loginFragment)
        }
        alertDialogBuilder.setNegativeButton("Cancel") { _, _ -> }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun getNotes() {
        homeViewModel.getNotes()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.notesState.collect { notesResult ->
                    when (notesResult) {
                        is RequestState.Success -> {
                            binding.progressBar.isVisible = false
                            Log.e("Success", notesResult.data.toString()+ notesResult.data.indices)
                            notesList = notesResult.data
                            initRecycler()
                            binding.swipeRefreshLayout.isRefreshing = false
                        }
                        is RequestState.Error -> {
                            binding.progressBar.isVisible = false
                            Log.e("Error", notesResult.exception.toString())
                        }
                        is RequestState.Loading -> {
                            binding.progressBar.isVisible = true
                            Log.e("Loading", "Loading")
                        }

                        null -> TODO()
                    }
                }
            }
        }
    }

    private fun initRecycler() {

        val toDoAdapter = ToDoListRecyclerAdapter(
            requireContext(),
            requireActivity(),
            notesList,
            object : OnClickListener {
                override fun onClick(position: Int) {
                    showUpdateDialog(notesList[position])
                }
            })
        binding.rvToDoList.adapter = toDoAdapter

        val swipeGesture = object : SwipeGesture(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        notesList[viewHolder.adapterPosition].id?.let { homeViewModel.deleteNote(it) }
                        getNotes()
                    }
                }
            }
        }
        ItemTouchHelper(swipeGesture).attachToRecyclerView(binding.rvToDoList)
    }

    @SuppressLint("MissingInflatedId")
    private fun showCreateDialog() {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.new_note, null)
        val mBuilder = AlertDialog.Builder(context).setView(mDialogView).show()

        val createButton = mDialogView.findViewById<AppCompatImageView>(R.id.btnCreate)
        val titleEditText = mDialogView.findViewById<AppCompatEditText>(R.id.etTitle)
        val descriptionEditText = mDialogView.findViewById<AppCompatEditText>(R.id.etDescription)

        createButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()
            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(context, "Please check the fields", Toast.LENGTH_SHORT).show()
            } else {
                homeViewModel.createNote(Note(auth.uid, title, description)) // Use the single 'auth' instance
                getNotes()
                mBuilder.dismiss()
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showUpdateDialog(note: Note) {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.update_note_dialog, null)
        val mBuilder = AlertDialog.Builder(context).setView(mDialogView).show()

        val createButton = mDialogView.findViewById<AppCompatImageView>(R.id.btnUpdate)
        val titleEditText = mDialogView.findViewById<AppCompatEditText>(R.id.etTitle)
        val descriptionEditText = mDialogView.findViewById<AppCompatEditText>(R.id.etDescription)

        titleEditText.setText(
            note.title.toString(),
            TextView.BufferType.EDITABLE
        )
        descriptionEditText.setText(
            note.description.toString(),
            TextView.BufferType.EDITABLE
        )
        createButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()
            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(context, "Please check the fields", Toast.LENGTH_SHORT).show()
            } else {
                val updatedNote = Note(note.id, title, description)
                homeViewModel.updateNote(updatedNote)
                getNotes()
                mBuilder.dismiss()
            }
        }
    }

    private fun onBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity?.finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

}