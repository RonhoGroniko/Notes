package com.sharapov.notes.domain.usecases

import com.sharapov.notes.domain.entities.ContentItem
import com.sharapov.notes.domain.repository.NotesRepository
import javax.inject.Inject

class AddNoteUseCase @Inject constructor (
    private val repository: NotesRepository
) {

    suspend operator fun invoke(title: String, content: List<ContentItem>) {
        repository.addNote(
            title = title,
            content = content,
            isPinned = false,
            updatedAt = System.currentTimeMillis()
        )
    }
}
