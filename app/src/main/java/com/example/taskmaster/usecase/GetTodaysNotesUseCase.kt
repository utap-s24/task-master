package com.example.taskmaster.usecase

import com.example.taskmaster.data.FirestoreRepository
import com.example.taskmaster.data.RequestState
import kotlinx.coroutines.flow.flow
import java.lang.Exception

class GetTodaysNotesUseCase constructor(private val repository: FirestoreRepository) {

    suspend fun invoke(currentDate: String) = flow {
        try {
            emit(RequestState.Loading())
            emit(RequestState.Success(repository.getTodaysNotes(currentDate)))
        } catch (e: Exception) {
            emit(RequestState.Error(e))
        }
    }
}