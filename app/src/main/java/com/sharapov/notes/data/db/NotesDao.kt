package com.sharapov.notes.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sharapov.notes.data.db.models.ContentItemDbModel
import com.sharapov.notes.data.db.models.NoteDbModel
import com.sharapov.notes.data.db.models.NoteWithContentDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes() : Flow<List<NoteWithContentDbModel>>

    @Query("SELECT * FROM notes WHERE id =:noteId LIMIT 1")
    suspend fun getNote(noteId: Int): NoteWithContentDbModel

    @Query("""
        SELECT DISTINCT notes.* FROM notes JOIN content
        ON notes.id == content.noteId WHERE title 
        LIKE '%' || :query || '%' OR content 
        LIKE '%' || :query || '%' 
        ORDER BY updatedAt DESC
        """)
    fun searchNotes(query: String) : Flow<List<NoteWithContentDbModel>>

    @Query("UPDATE notes SET isPinned = NOT isPinned WHERE id == :noteId")
    suspend fun switchPinnedStatus(noteId: Int)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addNote(noteDbModel: NoteDbModel): Long

    @Query("DELETE FROM notes WHERE id == :noteId")
    suspend fun deleteNote(noteId: Int)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addNoteContent(contentItemDbModel: List<ContentItemDbModel>)

    @Query("DELETE FROM content WHERE noteId == :noteId")
    suspend fun deleteNoteContent(noteId: Int)
}