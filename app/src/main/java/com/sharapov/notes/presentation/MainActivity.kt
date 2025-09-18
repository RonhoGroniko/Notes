package com.sharapov.notes.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sharapov.notes.presentation.navigation.NavGraph
import com.sharapov.notes.presentation.screens.editing.EditNoteScreen
import com.sharapov.notes.presentation.ui.theme.NotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotesTheme {
                NavGraph()
            }
        }
    }
}

