package com.example.taskmaster.usecase

import com.example.taskmaster.data.FirestoreRepository
import com.example.taskmaster.data.RequestState
import kotlinx.coroutines.flow.flow
import java.lang.Exception

class GetFilterNotesUseCase constructor(private val repository: FirestoreRepository) {

    suspend fun invoke(priority: Boolean, category: String) = flow {
        try {
            emit(RequestState.Loading())
            emit(RequestState.Success(repository.getFilterNotes(priority, category)))
        } catch (e: Exception) {
            emit(RequestState.Error(e))
        }
    }
}