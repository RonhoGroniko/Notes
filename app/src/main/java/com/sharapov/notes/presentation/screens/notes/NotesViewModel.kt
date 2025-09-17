package com.sharapov.notes.presentation.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharapov.notes.data.TestNotesRepositoryImpl
import com.sharapov.notes.domain.AddNoteUseCase
import com.sharapov.notes.domain.DeleteNoteUseCase
import com.sharapov.notes.domain.EditNoteUseCase
import com.sharapov.notes.domain.GetAllNotesUseCase
import com.sharapov.notes.domain.GetNoteUseCase
import com.sharapov.notes.domain.Note
import com.sharapov.notes.domain.SearchNotesUseCase
import com.sharapov.notes.domain.SwitchPinnedStatusUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel : ViewModel() {

    private val repository = TestNotesRepositoryImpl

    private val addNoteUseCase = AddNoteUseCase(repository)
    private val deleteNoteUseCase = DeleteNoteUseCase(repository)
    private val editNoteUseCase = EditNoteUseCase(repository)
    private val getAllNotesUseCase = GetAllNotesUseCase(repository)
    private val getNoteUseCase = GetNoteUseCase(repository)
    private val searchNotesUseCase = SearchNotesUseCase(repository)
    private val switchPinnedStatusUseCase = SwitchPinnedStatusUseCase(repository)

    private val _state =
        MutableStateFlow(NotesScreenState(pinnedNotes = listOf(), unpinnedNotes = listOf()))
    val state = _state.asStateFlow()

    private val query = MutableStateFlow("")


    init {
        query
            .flatMapLatest {
                if (it.isBlank()) {
                    getAllNotesUseCase()
                } else {
                    searchNotesUseCase(it)
                }
            }
            .onEach { list ->
                val pinned = list.filter { it.isPinned }
                val unpinned = list.filter { !it.isPinned }
                _state.value = NotesScreenState(
                    query = query.value,
                    pinnedNotes = pinned,
                    unpinnedNotes = unpinned
                )
            }
            .launchIn(viewModelScope)
    }

    fun processCommand(command: NotesCommand) {
        when (command) {

            is NotesCommand.DeleteNote -> {
                deleteNoteUseCase(command.id)
            }

            is NotesCommand.EditNote -> {
                val title = command.note.title
                editNoteUseCase(command.note.copy(title = "$title edited"))
            }

            is NotesCommand.SwitchPinnedStatus -> {
                switchPinnedStatusUseCase(command.id)
            }

            is NotesCommand.InputSearchQuery -> {

            }
        }
    }
}

sealed interface NotesCommand {

    data class InputSearchQuery(val query: String) : NotesCommand

    data class SwitchPinnedStatus(val id: Int) : NotesCommand

    // Temp
    data class DeleteNote(val id: Int) : NotesCommand
    data class EditNote(val note: Note) : NotesCommand
}

data class NotesScreenState(
    val query: String = "",
    val pinnedNotes: List<Note>,
    val unpinnedNotes: List<Note>
)