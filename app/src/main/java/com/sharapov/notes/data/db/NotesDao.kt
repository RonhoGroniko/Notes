package com.sharapov.notes.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sharapov.notes.data.db.NoteDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes() : Flow<List<NoteDbModel>>

    @Query("SELECT * FROM notes WHERE id =:noteId LIMIT 1")
    suspend fun getNote(noteId: Int): NoteDbModel

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchNotes(query: String) : Flow<List<NoteDbModel>>

    @Query("UPDATE notes SET isPinned = NOT isPinned WHERE id == :noteId")
    suspend fun switchPinnedStatus(noteId: Int)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addNote(noteDbModel: NoteDbModel)

    @Query("DELETE FROM notes WHERE id == :noteId")
    suspend fun deleteNote(noteId: Int)
}