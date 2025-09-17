package com.sharapov.notes.presentation.screens.notes


import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharapov.notes.domain.Note
import com.sharapov.notes.presentation.ui.theme.Green
import com.sharapov.notes.presentation.ui.theme.Yellow100

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
            Title(
                text = "All Notes"
            )
        }
        item {
            SearchBar(
                query = state.query,
                onQueryChange = {
                    viewModel.processCommand(NotesCommand.InputSearchQuery(it))
                }
            )
        }
        item {
            Subtitle(
                text = "Pinned"
            )
        }
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
                        background = Yellow100,
                        onNoteClick = {
                            viewModel.processCommand(NotesCommand.EditNote(it))
                        },
                        onDoubleClick = {
                            viewModel.processCommand(NotesCommand.DeleteNote(it.id))
                        },
                        onLongClick = {
                            viewModel.processCommand(NotesCommand.SwitchPinnedStatus(it.id))
                        },
                    )
                }
            }
        }
        item {
            Subtitle(
                text = "Unpinned"
            )
        }
        items(
            items = state.unpinnedNotes,
            key = { it.id }
        ) { note ->
            NoteCard(
                note = note,
                background = Green,
                onNoteClick = {
                    viewModel.processCommand(NotesCommand.EditNote(it))
                },
                onDoubleClick = {
                    viewModel.processCommand(NotesCommand.DeleteNote(it.id))
                },
                onLongClick = {
                    viewModel.processCommand(NotesCommand.SwitchPinnedStatus(it.id))
                },
            )
        }
    }
}


@Composable
fun Title(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = 24.sp,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Search...",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Notes"
            )
        },
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun Subtitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}


@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    note: Note,
    background: Color,
    onNoteClick: (Note) -> Unit,
    onDoubleClick: (Note) -> Unit,
    onLongClick: (Note) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .combinedClickable(
                onClick = {
                    onNoteClick(note)
                },
                onDoubleClick = {
                    onDoubleClick(note)
                },
                onLongClick = {
                    onLongClick(note)
                }
            )
    ) {
        Text(
            text = note.title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = note.updatedAt.toString(),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = note.content,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}