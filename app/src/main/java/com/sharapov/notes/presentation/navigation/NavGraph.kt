package com.sharapov.notes.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.sharapov.notes.presentation.screens.creation.CreateNoteScreen
import com.sharapov.notes.presentation.screens.editing.EditNoteScreen
import com.sharapov.notes.presentation.screens.notes.NotesScreen

@Composable
fun NavGraph() {
    val screen = remember {
        mutableStateOf<Screen>(Screen.Notes)
    }
    when(val currentState = screen.value) {
        Screen.CreateNote -> {
            CreateNoteScreen(
                onFinished = {
                    screen.value = Screen.Notes
                }
            )
        }
        is Screen.EditNote -> {
            EditNoteScreen(
                noteId = currentState.id,
                onFinished = {
                    screen.value = Screen.Notes
                }
            )
        }
        Screen.Notes -> {
            NotesScreen(
                onFABClick = {
                    screen.value = Screen.CreateNote
                },
                onNoteClick = {
                    screen.value = Screen.EditNote(it.id)
                }
            )
        }
    }
}


sealed interface Screen {

    data object Notes: Screen
    data object CreateNote: Screen
    data class EditNote(val id: Int): Screen
}
