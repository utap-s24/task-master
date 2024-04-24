package com.example.taskmaster.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taskmaster.usecase.CreateNoteUseCase
import com.example.taskmaster.usecase.DeleteNoteUseCase
import com.example.taskmaster.usecase.GetNotesUseCase
import com.example.taskmaster.usecase.UpdateNoteUseCase

class HomeViewModelFactory(
    private val createNoteUseCase: CreateNoteUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(createNoteUseCase, getNotesUseCase, updateNoteUseCase, deleteNoteUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
