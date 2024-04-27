package com.example.taskmaster.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taskmaster.usecase.GetFilterNotesUseCase
import com.example.taskmaster.usecase.DeleteNoteUseCase
import com.example.taskmaster.usecase.GetTodaysNotesUseCase
import com.example.taskmaster.usecase.UpdateNoteUseCase

class TodayViewModelFactory(
    private val getTodaysNotesUseCase: GetTodaysNotesUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val getFilterNotesUseCase: GetFilterNotesUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodayViewModel::class.java)) {
            return TodayViewModel(getTodaysNotesUseCase, updateNoteUseCase,
                deleteNoteUseCase, getFilterNotesUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
