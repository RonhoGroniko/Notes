package com.sharapov.notes.presentation.screens.creation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharapov.notes.domain.entities.ContentItem
import com.sharapov.notes.domain.usecases.AddNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(private val addNoteUseCase: AddNoteUseCase) :
    ViewModel() {


    private val _state = MutableStateFlow<CreateNoteState>(CreateNoteState.Creation())
    val state = _state.asStateFlow()


    fun processCommand(command: CreateNoteCommand) {
        when (command) {
            CreateNoteCommand.Back -> {
                _state.update { CreateNoteState.Finish }
            }

            is CreateNoteCommand.InputContent -> {
                _state.update { previousState ->
                    if (previousState is CreateNoteState.Creation) {
                        val newContent = previousState.content.mapIndexed { index, contentItem ->
                            if (contentItem is ContentItem.Text && command.index == index) {
                                contentItem.copy(content = command.content)
                            } else {
                                contentItem
                            }
                        }
                        previousState.copy(content = newContent)
                    } else {
                        previousState
                    }
                }
            }

            is CreateNoteCommand.InputTitle -> {
                _state.update { previousState ->
                    if (previousState is CreateNoteState.Creation) {
                        previousState.copy(
                            title = command.title,
                        )
                    } else {
                        previousState
                    }
                }
            }

            CreateNoteCommand.Save -> {
                viewModelScope.launch {
                    _state.update { previousState ->
                        if (previousState is CreateNoteState.Creation) {
                            val title = previousState.title
                            val content = previousState.content.filter {
                                it !is ContentItem.Text || it.content.isNotBlank()
                            }
                            addNoteUseCase(
                                title = title,
                                content = content
                            )
                            CreateNoteState.Finish
                        } else {
                            previousState
                        }
                    }
                }
            }

            is CreateNoteCommand.AddImage -> {
                _state.update { previousState ->
                    if (previousState is CreateNoteState.Creation) {
                        previousState.content.toMutableList().apply {
                            val lastItem = last()
                            if (lastItem is ContentItem.Text && lastItem.content.isBlank()) {
                                removeAt(lastIndex)
                            }
                            add(ContentItem.Image(command.uri.toString()))
                            add(ContentItem.Text(""))
                        }.let {
                            previousState.copy(content = it)
                        }
                    } else {
                        previousState
                    }
                }
            }
        }
    }
}

sealed interface CreateNoteCommand {

    data class InputTitle(val title: String) : CreateNoteCommand
    data class InputContent(val index: Int, val content: String) : CreateNoteCommand
    data class AddImage(val uri: Uri) : CreateNoteCommand

    data object Save : CreateNoteCommand
    data object Back : CreateNoteCommand
}

sealed interface CreateNoteState {

    data class Creation(
        val title: String = "",
        val content: List<ContentItem> = listOf(ContentItem.Text(""))
    ) : CreateNoteState {

        val isSavable: Boolean
            get() {
                return when {
                    title.isBlank() -> false
                    content.isEmpty() -> false
                    else -> {
                        content.any {
                            it !is ContentItem.Text || it.content.isNotBlank()
                        }
                    }
                }
            }
    }

    data object Finish : CreateNoteState
}