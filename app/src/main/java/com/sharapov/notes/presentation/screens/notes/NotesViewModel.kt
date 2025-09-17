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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel : ViewModel() {

    private val repository = TestNotesRepositoryImpl

    private val getAllNotesUseCase = GetAllNotesUseCase(repository)
    private val searchNotesUseCase = SearchNotesUseCase(repository)
    private val switchPinnedStatusUseCase = SwitchPinnedStatusUseCase(repository)

    private val _state =
        MutableStateFlow(NotesScreenState(pinnedNotes = listOf(), unpinnedNotes = listOf()))
    val state = _state.asStateFlow()

    private val query = MutableStateFlow("")


    init {
        query
            .onEach { input ->
                _state.update { it.copy(query = input) }
            }
            .flatMapLatest { input ->
                if (input.isBlank()) {
                    getAllNotesUseCase()
                } else {
                    searchNotesUseCase(input)
                }
            }
            .onEach { notes ->
                val pinned = notes.filter { it.isPinned }
                val unpinned = notes.filter { !it.isPinned }
                _state.update {
                    it.copy(
                        query = query.value,
                        pinnedNotes = pinned,
                        unpinnedNotes = unpinned
                    )
                }
            }
            .launchIn(viewModelScope)
    }


    fun processCommand(command: NotesCommand) {
        viewModelScope.launch {
            when (command) {

                is NotesCommand.SwitchPinnedStatus -> {
                    switchPinnedStatusUseCase(command.id)
                }

                is NotesCommand.InputSearchQuery -> {
                    query.update { command.query.trim() }
                }
            }
        }
    }
}

sealed interface NotesCommand {

    data class InputSearchQuery(val query: String) : NotesCommand

    data class SwitchPinnedStatus(val id: Int) : NotesCommand

}

data class NotesScreenState(
    val query: String = "",
    val pinnedNotes: List<Note>,
    val unpinnedNotes: List<Note>
)