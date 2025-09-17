package com.sharapov.notes.domain

class GetNoteUseCase(
    private val repository: NotesRepository
) {

    operator fun invoke(id: Int): Note {
        return repository.getNote(id)
    }
}
