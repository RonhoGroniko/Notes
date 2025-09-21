package com.sharapov.notes.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sharapov.notes.data.db.models.ContentItemDbModel
import com.sharapov.notes.data.db.models.NoteDbModel
import com.sharapov.notes.data.db.models.NoteWithContentDbModel
import com.sharapov.notes.data.mapper.toContentItemDbModels
import com.sharapov.notes.domain.entities.ContentItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Transaction
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes() : Flow<List<NoteWithContentDbModel>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id =:noteId LIMIT 1")
    suspend fun getNote(noteId: Int): NoteWithContentDbModel

    @Transaction
    @Query("""
        SELECT DISTINCT notes.* FROM notes JOIN content
        ON notes.id == content.noteId WHERE title 
        LIKE '%' || :query || '%' OR content 
        LIKE '%' || :query || '%' AND contentType == 'TEXT'
        ORDER BY updatedAt DESC
        """)
    fun searchNotes(query: String) : Flow<List<NoteWithContentDbModel>>

    @Query("UPDATE notes SET isPinned = NOT isPinned WHERE id == :noteId")
    suspend fun switchPinnedStatus(noteId: Int)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addNote(noteDbModel: NoteDbModel): Long

    @Transaction
    @Query("DELETE FROM notes WHERE id == :noteId")
    suspend fun deleteNote(noteId: Int)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addNoteContent(contentItemDbModel: List<ContentItemDbModel>)

    @Query("DELETE FROM content WHERE noteId == :noteId")
    suspend fun deleteNoteContent(noteId: Int)

    @Transaction
    suspend fun addNoteWithContent(
        noteDbModel: NoteDbModel,
        content: List<ContentItem>
    ) {
        val noteId = addNote(noteDbModel).toInt()
        addNoteContent(content.toContentItemDbModels(noteId))
    }

    @Transaction
    suspend fun updateNote(
        noteDbModel: NoteDbModel,
        content: List<ContentItemDbModel>
    ) {
        addNote(noteDbModel)
        deleteNoteContent(noteDbModel.id)
        addNoteContent(content)
    }
}