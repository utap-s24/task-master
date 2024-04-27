package com.example.taskmaster.data

import com.example.taskmaster.data.Note


interface FirestoreRepository {

    suspend fun createNote(note: Note)
    suspend fun getNotes(): ArrayList<Note>
    suspend fun getTodaysNotes(currentDate: String): ArrayList<Note>
    suspend fun getFilterNotes(priority: Boolean, category: String): ArrayList<Note>
    suspend fun getFilterTodayNotes(priority: Boolean, category: String, date: String): ArrayList<Note>
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(docId: String)
    suspend fun getCount(): ArrayList<Int>
}