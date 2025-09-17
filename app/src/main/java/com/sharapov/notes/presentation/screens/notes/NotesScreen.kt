package com.sharapov.notes.presentation.screens.notes


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharapov.notes.domain.Note

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = modifier
            .padding(top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = state.pinnedNotes,
                    key = { it.id }
                ) { note ->
                    NoteCard(
                        note = note,
                        onNoteClick = {
                            viewModel.processCommand(NotesCommand.SwitchPinnedStatus(note.id))
                        }
                    )
                }
            }
        }
        items(
            items = state.unpinnedNotes,
            key = { it.id }
        ) { note ->
            NoteCard(
                note = note,
                onNoteClick = {
                    viewModel.processCommand(NotesCommand.SwitchPinnedStatus(note.id))
                }
            )
        }
    }
}


@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    note: Note,
    onNoteClick: () -> Unit
) {
    Text(
        modifier = modifier.clickable(true) {
            onNoteClick()
        },
        text = "${note.title} - ${note.content}",
        fontSize = 24.sp
    )
}