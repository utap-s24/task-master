package com.example.taskmaster.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taskmaster.usecase.GetCategoryCount

class UserProfileViewModelFactory(
    private val getCount: GetCategoryCount
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodayViewModel::class.java)) {
            return UserProfileViewModel(getCount) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
