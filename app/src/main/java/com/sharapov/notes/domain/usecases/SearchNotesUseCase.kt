package com.sharapov.notes.domain.usecases

import com.sharapov.notes.domain.entities.Note
import com.sharapov.notes.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchNotesUseCase @Inject constructor (
    private val repository: NotesRepository
) {

    operator fun invoke(query: String): Flow<List<Note>> {
        return repository.searchNotes(query)
    }
}
