package com.sharapov.notes.presentation.screens.editing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharapov.notes.domain.entities.ContentItem
import com.sharapov.notes.domain.entities.Note
import com.sharapov.notes.domain.usecases.DeleteNoteUseCase
import com.sharapov.notes.domain.usecases.EditNoteUseCase
import com.sharapov.notes.domain.usecases.GetNoteUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EditNoteViewModel.Factory::class)
class EditNoteViewModel @AssistedInject constructor(
    @Assisted("id") private val id: Int,
    private val editNoteUseCase: EditNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val getNoteUseCase: GetNoteUseCase
) :
    ViewModel() {

    private val _state = MutableStateFlow<EditNoteState>(EditNoteState.Initial)
    val state = _state.asStateFlow()


    init {
        viewModelScope.launch {
            _state.update {
                val note = getNoteUseCase(id)
                EditNoteState.Editing(note)
            }
        }
    }

    fun processCommand(command: EditNoteCommand) {
        when (command) {
            EditNoteCommand.Back -> {
                _state.update { EditNoteState.Finish }
            }

            is EditNoteCommand.InputContent -> {
                _state.update { previousState ->
                    if (previousState is EditNoteState.Editing) {
                        val newContent = ContentItem.Text(content = command.content)
                        val newNote = previousState.note.copy(content = listOf(newContent))
                        EditNoteState.Editing(note = newNote)
                    } else {
                        previousState
                    }
                }
            }

            is EditNoteCommand.InputTitle -> {
                _state.update { previousState ->
                    if (previousState is EditNoteState.Editing) {
                        val newNote = previousState.note.copy(title = command.title)
                        EditNoteState.Editing(note = newNote)
                    } else {
                        previousState
                    }
                }
            }

            EditNoteCommand.Save -> {
                viewModelScope.launch {
                    _state.update { previousState ->
                        if (previousState is EditNoteState.Editing) {
                            val note = previousState.note
                            editNoteUseCase(note)
                            EditNoteState.Finish
                        } else {
                            previousState
                        }
                    }
                }
            }

            EditNoteCommand.Delete -> {
                viewModelScope.launch {
                    _state.update { previousState ->
                        if (previousState is EditNoteState.Editing) {
                            val note = previousState.note
                            deleteNoteUseCase(note.id)
                            EditNoteState.Finish
                        } else {
                            previousState
                        }
                    }
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {

        fun create(@Assisted("id") id: Int): EditNoteViewModel
    }
}

sealed interface EditNoteCommand {

    data class InputTitle(val title: String) : EditNoteCommand
    data class InputContent(val content: String) : EditNoteCommand

    data object Save : EditNoteCommand
    data object Back : EditNoteCommand
    data object Delete : EditNoteCommand
}

sealed interface EditNoteState {

    data object Initial : EditNoteState

    data class Editing(
        val note: Note
    ) : EditNoteState {

        val isSavable: Boolean
            get() {
                return when {
                    note.title.isBlank() -> false
                    note.content.isEmpty() -> false
                    else -> {
                        note.content.any {
                            it !is ContentItem.Text || it.content.isNotBlank()
                        }
                    }
                }
            }

    }

    data object Finish : EditNoteState
}