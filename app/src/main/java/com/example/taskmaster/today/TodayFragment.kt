package com.example.taskmaster.today

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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
import com.example.taskmaster.data.FirestoreRepositoryImpl
import com.example.taskmaster.data.Note
import com.example.taskmaster.data.RequestState
import com.example.taskmaster.databinding.FragmentTodayBinding
import com.example.taskmaster.tasklist.ToDoListRecyclerAdapter
import com.example.taskmaster.tasklist.TodayViewModel
import com.example.taskmaster.tasklist.TodayViewModelFactory
import com.example.taskmaster.usecase.CreateNoteUseCase
import com.example.taskmaster.usecase.DeleteNoteUseCase
import com.example.taskmaster.usecase.GetTodaysNotesUseCase
import com.example.taskmaster.usecase.UpdateNoteUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

class TodayFragment : Fragment() {
    private lateinit var binding: FragmentTodayBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    private lateinit var notesList: ArrayList<Note>

//    val currentDate = getCurrentDate()

    // Use the 'auth' instance to instantiate FirestoreRepositoryImpl
    private val firestoreRepositoryImpl = FirestoreRepositoryImpl(FirebaseFirestore.getInstance(), auth)

    // Now use the firestoreRepositoryImpl to create your use cases
    private val createNoteUseCase = CreateNoteUseCase(firestoreRepositoryImpl)
    private val getNotesUseCase = GetTodaysNotesUseCase(firestoreRepositoryImpl)
    private val updateNoteUseCase = UpdateNoteUseCase(firestoreRepositoryImpl)
    private val deleteNoteUseCase = DeleteNoteUseCase(firestoreRepositoryImpl)


    // Create the ViewModel factory with your use cases
    private val factory = TodayViewModelFactory(createNoteUseCase, getNotesUseCase, updateNoteUseCase, deleteNoteUseCase)

    // Use the factory to create the ViewModel
    private val todayViewModel by lazy { ViewModelProvider(this, factory).get(TodayViewModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodayBinding.inflate(inflater)


        binding.tvWelcome.text =
            "Welcome ${SharedPreferences(requireContext()).getUsernameString()}!"
        initListeners()
        getNotes()
//        onBackPressed()

        return binding.root
    }

    private fun initListeners() {
        with(binding) {
            ivLogout.setOnClickListener {
                initLogoutDialog()
            }
            swipeRefreshLayout.setOnRefreshListener {
                getNotes()
            }
        }
    }

    private fun getNotes() {
        todayViewModel.getNotes(getCurrentDate())
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                todayViewModel.notesState.collect { notesResult ->
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
                    //showUpdateDialog(notesList[position])
                }
            })
        binding.rvToDoList.adapter = toDoAdapter

        val swipeGesture = object : SwipeGesture(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        notesList[viewHolder.adapterPosition].id?.let { todayViewModel.deleteNote(it) }
                        getNotes()
                    }
                }
            }
        }
        ItemTouchHelper(swipeGesture).attachToRecyclerView(binding.rvToDoList)
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

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    private fun isSameDay(date1: String, date2: String): Boolean {
        return date1 == date2
    }
    // Here you can set up any specific logic for this fragment, such as loading today's tasks
}