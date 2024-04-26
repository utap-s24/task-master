package com.example.taskmaster.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.taskmaster.R
import com.example.taskmaster.SharedPreferences
import com.example.taskmaster.databinding.FragmentTodayBinding
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

class TodayFragment : Fragment() {
    private lateinit var binding: FragmentTodayBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodayBinding.inflate(inflater)


        binding.tvWelcome.text =
            "Welcome ${SharedPreferences(requireContext()).getUsernameString()}!"
//        initListeners()
//        getNotes()
//        onBackPressed()

        return binding.root
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