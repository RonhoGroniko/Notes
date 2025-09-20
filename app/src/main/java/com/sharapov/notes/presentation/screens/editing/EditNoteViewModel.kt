package com.sharapov.notes.presentation.screens.editing

import android.net.Uri
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
                if (note.content.last() is ContentItem.Image) {
                    note.content.toMutableList().apply {
                        add(ContentItem.Text(""))
                    }.let {
                        EditNoteState.Editing(note.copy(content = it))
                    }
                } else {
                    EditNoteState.Editing(note)
                }
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
                        val newContent = previousState.note.content.mapIndexed { index, contentItem ->
                            if (contentItem is ContentItem.Text && index == command.index) {
                                contentItem.copy(content = command.content)
                            } else {
                                contentItem
                            }
                        }
                        val newNote = previousState.note.copy(content = newContent)
                        previousState.copy(newNote)
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
                            val content = previousState.note.content.filter {
                                it !is ContentItem.Text || it.content.isNotBlank()
                            }
                            editNoteUseCase(note.copy(content = content))
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

            is EditNoteCommand.AddImage -> {
                _state.update {  previousState ->
                    if (previousState is EditNoteState.Editing) {
                        previousState.note.content.toMutableList().apply {
                            val lastItem = last()
                            if (lastItem is ContentItem.Text && lastItem.content.isBlank()) {
                                removeAt(lastIndex)
                            }
                            add(ContentItem.Image(command.uri.toString()))
                            add(ContentItem.Text(""))
                        }.let {
                            previousState.copy(previousState.note.copy(content = it))
                        }
                    } else {
                        previousState
                    }
                }
            }

            is EditNoteCommand.DeleteImage -> {
                _state.update { previousState ->
                    if (previousState is EditNoteState.Editing) {
                        previousState.note.content.toMutableList().apply {

                            if ( command.index == 0 || (this[command.index - 1] is ContentItem.Image || this[command.index + 1] is ContentItem.Image)) {
                                removeAt(command.index)
                            } else {
                                removeAt(command.index)
                                removeAt(command.index)
                            }
                        }.let {
                            previousState.copy(note = previousState.note.copy(content = it))
                        }
                    } else {
                        previousState
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
    data class InputContent(val index: Int, val content: String) : EditNoteCommand
    data class AddImage(val uri: Uri): EditNoteCommand
    data class DeleteImage(val index: Int) : EditNoteCommand

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