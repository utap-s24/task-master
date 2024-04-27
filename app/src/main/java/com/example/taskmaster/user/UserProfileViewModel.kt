package com.example.taskmaster.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.data.RequestState
import com.example.taskmaster.usecase.GetCategoryCount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class UserProfileViewModel(
    private val getCategoryCount: GetCategoryCount
) : ViewModel() {

    private val _count = MutableStateFlow<RequestState<ArrayList<Int>>?>(null)
    val countState: StateFlow<RequestState<ArrayList<Int>>?> = _count

    fun getCount() = viewModelScope.launch {
        getCategoryCount.invoke().collect {
            _count.value = it
        }
    }
}