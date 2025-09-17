package com.sharapov.notes.domain

class DeleteNoteUseCase(
    private val repository: NotesRepository
) {

    suspend operator fun invoke(id: Int) {
        repository.deleteNote(id)
    }
}
