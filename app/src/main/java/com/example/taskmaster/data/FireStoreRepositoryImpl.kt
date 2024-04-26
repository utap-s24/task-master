package com.example.taskmaster.data

import com.example.taskmaster.data.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.tasks.await

const val USERS = "users"
const val NOTES = "notes"

class FirestoreRepositoryImpl constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) :
    FirestoreRepository {

    override suspend fun createNote(note: Note) {
        auth.uid?.let {
            firebaseFirestore.collection(USERS).document(it).collection(NOTES).add(note).await()
        }
    }

    override suspend fun getNotes(): ArrayList<Note> {
        val noteList: ArrayList<Note> = arrayListOf()
        val def = CompletableDeferred<ArrayList<Note>>()
        auth.uid?.let { uid ->
            firebaseFirestore.collection(USERS).document(uid).collection(NOTES)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) it.result.let { note ->
                        val notesDocuments = note.documents
                        notesDocuments.forEach { noteDocument ->
                            val note = Note(noteDocument.id, noteDocument.getString("title"), noteDocument.getString("date"),
                                noteDocument.getString("category"), noteDocument.getString("priority"))
                            noteList.add(note)
                        }
                        def.complete(noteList)
                    }
                }
        }
        return def.await()
    }

    override suspend fun updateNote(note: Note) {
        auth.uid?.let {
            note.id?.let { it1 ->
                firebaseFirestore.collection(USERS).document(it).collection(NOTES).document(it1)
                    .update(mapOf(
                        "title" to note.title,
                        "date" to note.date,
                        "category" to note.category,
                        "priority" to note.priority

                    )).await()
            }
        }
    }

    override suspend fun deleteNote(docId: String) {
        auth.uid?.let {
            firebaseFirestore.collection(USERS).document(it).collection(NOTES).document(docId)
                .delete().await()
        }
    }

}