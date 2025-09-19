package com.sharapov.notes.domain.entities

data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val updatedAt: Long,
    val isPinned: Boolean
)