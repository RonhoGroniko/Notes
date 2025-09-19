package com.sharapov.notes.domain.usecases

import com.sharapov.notes.domain.repository.NotesRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor (
    private val repository: NotesRepository
) {

    suspend operator fun invoke(id: Int) {
        repository.deleteNote(id)
    }
}
