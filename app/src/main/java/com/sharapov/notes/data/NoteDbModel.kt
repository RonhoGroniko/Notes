package com.sharapov.notes.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class NoteDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val content: String,
    val updatedAt: Long,
    val isPinned: Boolean
)
