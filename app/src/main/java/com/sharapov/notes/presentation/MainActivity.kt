package com.sharapov.notes.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sharapov.notes.presentation.screens.editing.EditNoteScreen
import com.sharapov.notes.presentation.ui.theme.NotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotesTheme {
                EditNoteScreen(
                    noteId = 5,
                    onFinished = {
                        Log.d("MainActivity", "FINISHED")
                    }
                )
//                CreateNoteScreen(
//                    onFinished = {
//                        Log.d("MainActivity", "FINISHED")
//                    }
//                )
//                NotesScreen(
//                    onFABClick = { Log.d("MainActivity", "Fab clicked") },
//                    onNoteClick = { Log.d("MainActivity", "$it") }
//                )
            }
        }
    }
}

