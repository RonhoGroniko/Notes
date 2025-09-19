package com.sharapov.notes.domain.usecases

import com.sharapov.notes.domain.entities.Note
import com.sharapov.notes.domain.repository.NotesRepository
import javax.inject.Inject

class GetNoteUseCase @Inject constructor (
    private val repository: NotesRepository
) {

    suspend operator fun invoke(id: Int): Note {
        return repository.getNote(id)
    }
}
