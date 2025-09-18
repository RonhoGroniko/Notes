package com.sharapov.notes.presentation.screens.editing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharapov.notes.data.TestNotesRepositoryImpl
import com.sharapov.notes.domain.DeleteNoteUseCase
import com.sharapov.notes.domain.EditNoteUseCase
import com.sharapov.notes.domain.GetNoteUseCase
import com.sharapov.notes.domain.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditNoteViewModel(private val id: Int) : ViewModel() {

    private val repository = TestNotesRepositoryImpl

    private val editNoteUseCase = EditNoteUseCase(repository)
    private val deleteNoteUseCase = DeleteNoteUseCase(repository)
    private val getNoteUseCase = GetNoteUseCase(repository)

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
                        val newNote = previousState.note.copy(content = command.content)
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
}

sealed interface EditNoteCommand {

    data class InputTitle(val title: String) : EditNoteCommand
    data class InputContent(val content: String) : EditNoteCommand

    data object Save : EditNoteCommand
    data object Back : EditNoteCommand
    data object Delete: EditNoteCommand
}

sealed interface EditNoteState {

    data object Initial: EditNoteState

    data class Editing(
       val note: Note
    ) : EditNoteState {

        val isSavable = note.content.isNotBlank() && note.title.isNotBlank()
    }

    data object Finish : EditNoteState
}