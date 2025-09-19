package com.sharapov.notes.presentation.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharapov.notes.domain.entities.Note
import com.sharapov.notes.domain.usecases.GetAllNotesUseCase
import com.sharapov.notes.domain.usecases.SearchNotesUseCase
import com.sharapov.notes.domain.usecases.SwitchPinnedStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val searchNotesUseCase: SearchNotesUseCase,
    private val switchPinnedStatusUseCase: SwitchPinnedStatusUseCase
) : ViewModel() {


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