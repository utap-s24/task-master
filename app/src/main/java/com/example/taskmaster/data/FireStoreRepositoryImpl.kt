package com.example.taskmaster.data

import com.example.taskmaster.data.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
                .orderBy("date", Query.Direction.ASCENDING)
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

     override suspend fun getTodaysNotes(currentDate: String): ArrayList<Note> {
         val todayList: ArrayList<Note> = arrayListOf()
         val def = CompletableDeferred<ArrayList<Note>>()
         auth.uid?.let { uid ->
             firebaseFirestore.collection(USERS).document(uid).collection(NOTES).whereEqualTo("date", currentDate)
                 .get()
                 .addOnCompleteListener {
                     if (it.isSuccessful) it.result.let { note ->
                         val notesDocuments = note.documents
                         notesDocuments.forEach { noteDocument ->
                             val note = Note(noteDocument.id, noteDocument.getString("title"), noteDocument.getString("date"),
                                 noteDocument.getString("category"), noteDocument.getString("priority"))
                             todayList.add(note)
                         }
                         def.complete(todayList)
                     }
                 }
         }
         return def.await()
    }

    override suspend fun getCount(): ArrayList<Int> {
        val countList: ArrayList<Int> = arrayListOf()
        val def = CompletableDeferred<ArrayList<Int>>()
        // Handle potential null UID scenario
        if (auth.uid == null) {
            // Log a warning or throw an exception based on your app's logic
            return def.await() // Return empty list or throw if necessary
        }

        val uid = auth.uid!! // Safe access after null check

        var query = FirebaseFirestore.getInstance()
            .collection(USERS)
            .document(uid)
            .collection(NOTES)
            .whereEqualTo("category", "Work")

        var countQuery = query.count()
        val workCount = countQuery.get(AggregateSource.SERVER).result.count.toInt()

        query = FirebaseFirestore.getInstance()
            .collection(USERS)
            .document(uid)
            .collection(NOTES)
            .whereEqualTo("category", "Home")

        countQuery = query.count()
        val homeCount = countQuery.get(AggregateSource.SERVER).result.count.toInt()

        query = FirebaseFirestore.getInstance()
            .collection(USERS)
            .document(uid)
            .collection(NOTES)
            .whereEqualTo("category", "School")

        countQuery = query.count()
        val schoolCount = countQuery.get(AggregateSource.SERVER).result.count.toInt()
        countList.add(workCount)
        countList.add(homeCount)
        countList.add(schoolCount)
        def.complete(countList)
        return def.await()
    }


    override suspend fun getFilterNotes(priority: Boolean, category: String): ArrayList<Note> {
        val filterList: ArrayList<Note> = arrayListOf()
        val deferred = CompletableDeferred<ArrayList<Note>>()

        // Handle potential null UID scenario
        if (auth.uid == null) {
            // Log a warning or throw an exception based on your app's logic
            return deferred.await() // Return empty list or throw if necessary
        }

        val uid = auth.uid!! // Safe access after null check

        var notesRef: Query = FirebaseFirestore.getInstance()
            .collection(USERS)
            .document(uid)
            .collection(NOTES)
//            .orderBy("date", Query.Direction.ASCENDING)

        if (priority) {
            notesRef = notesRef.whereEqualTo("priority", "Yes")
        }

        if (category != "None") {
            notesRef = notesRef.whereEqualTo("category", category)
        }

        notesRef.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let { querySnapshot ->
                        querySnapshot.forEach { documentSnapshot ->
                            val note = Note(
                                documentSnapshot.id,
                                documentSnapshot.getString("title"),
                                documentSnapshot.getString("date"),
                                documentSnapshot.getString("category"),
                                documentSnapshot.getString("priority")
                            )
                            filterList.add(note)
                        }
                        deferred.complete(filterList)
                        println("List: " + filterList)
                    }
                }
            }

        return deferred.await()
    }

    override suspend fun getFilterTodayNotes(priority: Boolean, category: String, date: String): ArrayList<Note> {
        val filterList: ArrayList<Note> = arrayListOf()
        val deferred = CompletableDeferred<ArrayList<Note>>()

        // Handle potential null UID scenario
        if (auth.uid == null) {
            // Log a warning or throw an exception based on your app's logic
            return deferred.await() // Return empty list or throw if necessary
        }

        val uid = auth.uid!! // Safe access after null check

        var notesRef: Query = FirebaseFirestore.getInstance()
            .collection(USERS)
            .document(uid)
            .collection(NOTES)
            .whereEqualTo("date", date)

        if (priority) {
            notesRef = notesRef.whereEqualTo("priority", "Yes")
        }

        if (category != "None") {
            notesRef = notesRef.whereEqualTo("category", category)
        }

        notesRef.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let { querySnapshot ->
                        querySnapshot.forEach { documentSnapshot ->
                            val note = Note(
                                documentSnapshot.id,
                                documentSnapshot.getString("title"),
                                documentSnapshot.getString("date"),
                                documentSnapshot.getString("category"),
                                documentSnapshot.getString("priority")
                            )
                            filterList.add(note)
                        }
                        deferred.complete(filterList)
                    }
                }
            }

        return deferred.await()
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