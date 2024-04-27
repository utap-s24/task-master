package com.example.taskmaster.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.data.Note
import com.example.taskmaster.data.RequestState
import com.example.taskmaster.usecase.CreateNoteUseCase
import com.example.taskmaster.usecase.DeleteNoteUseCase
import com.example.taskmaster.usecase.GetNotesUseCase
import com.example.taskmaster.usecase.GetFilterNotesUseCase
import com.example.taskmaster.usecase.UpdateNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class HomeViewModel(
    private val createNoteUseCase: CreateNoteUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val getFilterNotesUseCase: GetFilterNotesUseCase
) : ViewModel() {

    private val _notesState = MutableStateFlow<RequestState<ArrayList<Note>>?>(null)
    val notesState: StateFlow<RequestState<ArrayList<Note>>?> = _notesState

    fun createNote(note: Note) = viewModelScope.launch {
        createNoteUseCase.invoke(note).collect {
        }
    }

    fun getNotes() = viewModelScope.launch {
        getNotesUseCase.invoke().collect {
            _notesState.value = it
        }
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        updateNoteUseCase.invoke(note).collect {
        }
    }

    fun deleteNote(docId: String) = viewModelScope.launch {
        deleteNoteUseCase.invoke(docId).collect {
        }
    }

    fun getFilterNotes(priority: Boolean, category: String) = viewModelScope.launch {
        getFilterNotesUseCase.invoke(priority, category).collect {
            _notesState.value = it
        }
    }

}