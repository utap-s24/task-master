package com.example.taskmaster.usecase

import com.example.taskmaster.data.FirestoreRepository
import com.example.taskmaster.data.RequestState
import kotlinx.coroutines.flow.flow
import java.lang.Exception

class GetCategoryCount constructor(private val repository: FirestoreRepository) {

    suspend fun invoke() = flow {
        try {
            emit(RequestState.Loading())
            emit(RequestState.Success(repository.getCount()))
        } catch (e: Exception) {
            emit(RequestState.Error(e))
        }
    }
}