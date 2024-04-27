package com.example.taskmaster.usecase

import com.example.taskmaster.data.FirestoreRepository
import com.example.taskmaster.data.RequestState
import kotlinx.coroutines.flow.flow
import java.lang.Exception

class GetFilterTodayNotesUseCase constructor(private val repository: FirestoreRepository) {

    suspend fun invoke(priority: Boolean, category: String, date: String) = flow {
        try {
            emit(RequestState.Loading())
            emit(RequestState.Success(repository.getFilterTodayNotes(priority, category, date)))
        } catch (e: Exception) {
            emit(RequestState.Error(e))
        }
    }
}