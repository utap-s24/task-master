package com.example.taskmaster.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taskmaster.usecase.CreateNoteUseCase
import com.example.taskmaster.usecase.DeleteNoteUseCase
import com.example.taskmaster.usecase.GetTodaysNotesUseCase
import com.example.taskmaster.usecase.UpdateNoteUseCase

class TodayViewModelFactory(
    private val createNoteUseCase: CreateNoteUseCase,
    private val getTodaysNotesUseCase: GetTodaysNotesUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodayViewModel::class.java)) {
            return TodayViewModel(createNoteUseCase, getTodaysNotesUseCase, updateNoteUseCase, deleteNoteUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
