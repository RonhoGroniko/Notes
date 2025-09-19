package com.sharapov.notes.data.repository

import com.sharapov.notes.domain.entities.Note
import com.sharapov.notes.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

object TestNotesRepositoryImpl : NotesRepository {

    private val testData = mutableListOf<Note>().apply {
        repeat(10) {
            add(Note(it, "Title $it", "Content $it",System.currentTimeMillis(), false))
        }
    }

    private val notesListFlow = MutableStateFlow<List<Note>>(testData)


    override suspend fun addNote(title: String, content: String, isPinned: Boolean, updatedAt: Long) {
        notesListFlow.update { oldList ->
            val note = Note(
                id = oldList.size,
                title = title,
                content = content,
                updatedAt = updatedAt,
                isPinned = isPinned
            )
            oldList + note
        }
    }

    override suspend fun deleteNote(id: Int) {
        notesListFlow.update { oldList ->
            oldList.toMutableList().apply {
                removeIf {
                    it.id == id
                }
            }
        }
    }

    override suspend fun editNote(note: Note) {
        notesListFlow.update { oldList ->
            oldList.map {
                if (it.id == note.id) {
                    note
                }
                else {
                    it
                }
            }
        }
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return notesListFlow
    }

    override suspend fun getNote(id: Int): Note {
        return notesListFlow.value.first { it.id == id }
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return notesListFlow.map { currentList ->
            currentList.filter {
                it.title.contains(query) || it.content.contains(query)
            }
        }
    }

    override suspend fun switchPinnedStatus(id: Int) {
        notesListFlow.update { oldList ->
            oldList.map {
                if (it.id == id) {
                    it.copy(isPinned = !it.isPinned)
                }
                else {
                    it
                }
            }
        }
    }
}