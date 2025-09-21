package com.sharapov.notes.data.repository

import com.sharapov.notes.data.db.NotesDao
import com.sharapov.notes.data.db.models.NoteDbModel
import com.sharapov.notes.data.internal_storage.ImageFileManager
import com.sharapov.notes.data.mapper.toContentItemDbModels
import com.sharapov.notes.data.mapper.toDbModel
import com.sharapov.notes.data.mapper.toEntities
import com.sharapov.notes.data.mapper.toEntity
import com.sharapov.notes.domain.entities.ContentItem
import com.sharapov.notes.domain.entities.Note
import com.sharapov.notes.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val notesDao: NotesDao,
    private val imageFileManager: ImageFileManager
) : NotesRepository {


    override suspend fun addNote(
        title: String,
        content: List<ContentItem>,
        isPinned: Boolean,
        updatedAt: Long
    ) {
        val processedContent = content.processToStorage()
        val noteDbModel = NoteDbModel(
            id = 0,
            title = title,
            updatedAt = updatedAt,
            isPinned = isPinned
        )
        val noteId = notesDao.addNote(noteDbModel).toInt()
        notesDao.addNoteContent(processedContent.toContentItemDbModels(noteId))
    }

    override suspend fun deleteNote(id: Int) {
        val note = notesDao.getNote(id).toEntity()
        notesDao.deleteNote(id)

        note.content.filterIsInstance<ContentItem.Image>()
            .map { it.url }
            .forEach {
                imageFileManager.deleteImage(it)
            }
    }

    override suspend fun editNote(note: Note) {
        val oldNote = notesDao.getNote(note.id).toEntity()
        val oldUrls = oldNote.content.filterIsInstance<ContentItem.Image>().map { it.url }
        val newUrls = note.content.filterIsInstance<ContentItem.Image>().map { it.url }

        val removedUrls = oldUrls - newUrls
        removedUrls.forEach {
            imageFileManager.deleteImage(it)
        }

        val processedContent = note.content.processToStorage()
        val processedNote = note.copy(content = processedContent)

        notesDao.addNote(processedNote.toDbModel())
        notesDao.deleteNoteContent(note.id)
        notesDao.addNoteContent(processedContent.toContentItemDbModels(note.id))
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return notesDao.getAllNotes().map { it.toEntities() }
    }

    override suspend fun getNote(id: Int): Note {
        return notesDao.getNote(id).toEntity()
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return notesDao.searchNotes(query).map { it.toEntities() }
    }

    override suspend fun switchPinnedStatus(id: Int) {
        notesDao.switchPinnedStatus(id)
    }

    private suspend fun List<ContentItem>.processToStorage(): List<ContentItem> {
        return map { contentItem ->
            when (contentItem) {
                is ContentItem.Image -> {
                    if (imageFileManager.isInternal(contentItem.url)) {
                        contentItem
                    } else {
                        val internalPath =
                            imageFileManager.copyImageToInternalStorage(contentItem.url)
                        ContentItem.Image(internalPath)
                    }
                }

                is ContentItem.Text -> contentItem
            }
        }
    }
}