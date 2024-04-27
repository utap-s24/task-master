package com.example.taskmaster.today

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isNotEmpty
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
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
import com.example.taskmaster.usecase.DeleteNoteUseCase
import com.example.taskmaster.usecase.GetFilterNotesUseCase
import com.example.taskmaster.usecase.GetFilterTodayNotesUseCase
import com.example.taskmaster.usecase.GetTodaysNotesUseCase
import com.example.taskmaster.usecase.UpdateNoteUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.ParseException


class TodayFragment : Fragment() {
    private lateinit var binding: FragmentTodayBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    private lateinit var notesList: ArrayList<Note>

//    val currentDate = getCurrentDate()

    // Use the 'auth' instance to instantiate FirestoreRepositoryImpl
    private val firestoreRepositoryImpl = FirestoreRepositoryImpl(FirebaseFirestore.getInstance(), auth)

    // Now use the firestoreRepositoryImpl to create your use cases
    private val getNotesUseCase = GetTodaysNotesUseCase(firestoreRepositoryImpl)
    private val updateNoteUseCase = UpdateNoteUseCase(firestoreRepositoryImpl)
    private val deleteNoteUseCase = DeleteNoteUseCase(firestoreRepositoryImpl)
    private val getFilterTodayNotesUseCase = GetFilterTodayNotesUseCase(firestoreRepositoryImpl)

    // Create the ViewModel factory with your use cases
    private val factory = TodayViewModelFactory(getNotesUseCase, updateNoteUseCase,
        deleteNoteUseCase, getFilterTodayNotesUseCase)

    // Use the factory to create the ViewModel
    private val todayViewModel by lazy { ViewModelProvider(this, factory).get(TodayViewModel::class.java) }

    private val categoryItems = arrayOf("None", "Work", "Home", "School")

    private val weatherApiBaseUrl = "https://api.open-meteo.com/"

    // Retrofit service instance
    private val weatherService by lazy {
        Retrofit.Builder()
            .baseUrl(weatherApiBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodayBinding.inflate(inflater)


        binding.tvWelcome.text =
            "Welcome ${SharedPreferences(requireContext()).getUsernameString()}!"
        fetchWeatherData()
        initListeners()
        getNotes()
        onBackPressed()
        initSpinners()
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
            goButton.setOnClickListener {
                // call a function to apply the filters
                applyFilters()
            }

        }
    }

    private fun applyFilters() {
        val priority = binding.priorityCheckbox.isChecked
        val categoryIndex = binding.categorySpinner.selectedItemPosition

        getFilterNotes(priority, categoryItems[categoryIndex])
    }

    private fun initSpinners() {
        // Define your array of items

        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryItems)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = categoryAdapter

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

    private fun getFilterNotes(priority: Boolean, category: String) {
        todayViewModel.getFilterNotes(priority, category, getCurrentDate())
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

    @SuppressLint("MissingInflatedId")
    private fun showUpdateDialog(note: Note) {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.update_note_dialog, null)
        val mBuilder = AlertDialog.Builder(context).setView(mDialogView).show()

        val createButton = mDialogView.findViewById<AppCompatImageView>(R.id.btnCreate)
        val titleEditText = mDialogView.findViewById<AppCompatEditText>(R.id.etTitle)
        val categorySpinner = mDialogView.findViewById<AppCompatSpinner>(R.id.etCategory)
        val dateEditText = mDialogView.findViewById<AppCompatEditText>(R.id.etDate)
        val priorityCheckBox = mDialogView.findViewById<AppCompatCheckBox>(R.id.etPriority)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter


        titleEditText.setText(
            note.title.toString(),
            TextView.BufferType.EDITABLE
        )

        dateEditText.setText(
            note.date.toString(),
            TextView.BufferType.EDITABLE
        )

        // Set priority CheckBox
        priorityCheckBox.isChecked = note.priority == "Yes"

        val catPos = categoryItems.indexOf(note.category)
        if (catPos != -1) {
            categorySpinner.setSelection(catPos)
        }

        dateEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val enteredDate = dateEditText.text.toString()
                if (!isValidDate(enteredDate)) {
                    dateEditText.error = "Invalid date format. Please enter YYYY-MM-DD."
                }
            }
        }

        createButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val date = dateEditText.text.toString()
            val category = categoryItems[categorySpinner.selectedItemPosition]
            var priority = "No"
            if (priorityCheckBox.isChecked) {
                priority = "Yes"
            }
            if (title.isEmpty() || date.isEmpty() || category.isEmpty() || priority.isEmpty()) {
                Toast.makeText(context, "Please check the fields", Toast.LENGTH_SHORT).show()
            } else {
                val updatedNote = Note(note.id, title, date, category, priority)
                todayViewModel.updateNote(updatedNote)
                getNotes()
                mBuilder.dismiss()
            }
        }
    }


    private fun isValidDate(date: String): Boolean {
        // Implement your date validation logic here using libraries like SimpleDateFormat
        // This is a simplified example, you might want to consider libraries like LocalDate
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        dateFormat.isLenient = false
        try {
            dateFormat.parse(date)
            return true
        } catch (e: ParseException) {
            return false
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

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        println("date: " + dateFormat.format(calendar.time))
        return dateFormat.format(calendar.time)
    }

    private fun fetchWeatherData() {
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        val defaultLatLon = 0f // Default value if prefs not found
        val latitude = sharedPref.getFloat(getString(R.string.saved_latitude_key), defaultLatLon)
        val longitude = sharedPref.getFloat(getString(R.string.saved_longitude_key), defaultLatLon)

        Log.d("TodayFragment", "Latitude: $latitude, Longitude: $longitude")

        if (latitude != defaultLatLon && longitude != defaultLatLon) {
            // Now you have the actual lat and lon
            lifecycleScope.launch {
                try {
                    val response = weatherService.getCurrentWeather(
                        lat = latitude.toDouble(),
                        lon = longitude.toDouble(),
                        current = "temperature_2m,weather_code",
                        hourly = "temperature_2m,weather_code"
                    )
                    if (response.isSuccessful) {
                        response.body()?.let {
                            Log.d("TodayFragment", "Weather Data Retrieved: $it")
                            it.currentWeather?.let { weather ->
                                updateWeatherUI(weather)
                            } ?: Log.d("TodayFragment", "currentWeather is null")
                        } ?: Log.d("TodayFragment", "Response body is null")
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("TodayFragment", "Failed to fetch weather data: $errorBody")
                    }
                } catch (e: Exception) {
                    Log.e("TodayFragment", "Error fetching weather data", e)
                }
            }
        } else {
            Log.e("TodayFragment", "No location data available")
            // Handle the case where location data is not available...
        }
    }

    private fun updateWeatherUI(weather: CurrentWeather) {
        // Assuming you have ImageView for weather icons and TextViews for temperature & description
        val weatherDescription = mapWeatherCodeToDescription(weather.weather_code)
        Log.d("TodayFragment", "Weather Description: $weatherDescription")
        binding.weatherTemperature.text = "${weather.temperature_2m}°C"
        binding.weatherDescription.text = weatherDescription
        // Set weather icon based on weather code
        binding.weatherIcon.setImageResource(mapWeatherCodeToIcon(weather.weather_code))
    }
    private fun mapWeatherCodeToDescription(code: Int): String {
        // Map the weather code to a weather description
        return when (code) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            // Add other cases as per Open-Meteo documentation
            else -> "Unknown"
        }
    }
    private fun mapWeatherCodeToIcon(code: Int): Int {
        // Map the weather code to an icon resource
        return when (code) {
            0 -> R.drawable.ic_clear_sky
            1 -> R.drawable.ic_clear_sky //ic_mainly_clear
            2 -> R.drawable.ic_clear_sky //ic_partly_cloudy
            3 -> R.drawable.ic_clear_sky //ic_overcast
            // Add other cases and icons as needed
            else -> R.drawable.ic_clear_sky //ic_unknown_weather
        }
    }

    // Here you can set up any specific logic for this fragment, such as loading today's tasks
    // Retrofit interface
    interface WeatherService {
        @GET("v1/forecast")
        suspend fun getCurrentWeather(
            @Query("latitude") lat: Double,
            @Query("longitude") lon: Double,
            @Query("current") current: String,
            @Query("hourly") hourly: String
        ): Response<WeatherResponse>
    }

    data class WeatherResponse(
        @SerializedName("current_weather")
        val currentWeather: CurrentWeather
    )

    data class CurrentWeather(
        @SerializedName("temperature_2m") val temperature_2m: Double,
        @SerializedName("weather_code") val weather_code: Int
    )
}