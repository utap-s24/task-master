package com.example.taskmaster.usecase

import com.example.taskmaster.data.FirestoreRepository
import com.example.taskmaster.data.Note
import com.example.taskmaster.data.RequestState
import kotlinx.coroutines.flow.flow
import java.lang.Exception

class CreateNoteUseCase constructor(private val repository: FirestoreRepository) {

    suspend fun invoke(note: Note) = flow {
        try {
            emit(RequestState.Loading())
            emit(RequestState.Success(repository.createNote(note)))
        } catch (e: Exception) {
            emit(RequestState.Error(e))
        }
    }
}