package com.sharapov.notes.domain.usecases

import com.sharapov.notes.domain.entities.Note
import com.sharapov.notes.domain.repository.NotesRepository
import javax.inject.Inject

class EditNoteUseCase @Inject constructor (
    private val repository: NotesRepository
) {

    suspend operator fun invoke(note: Note) {
        repository.editNote(note.copy(updatedAt = System.currentTimeMillis()))
    }
}