package com.sharapov.notes.data

import com.sharapov.notes.domain.Note

fun Note.toDbModel(): NoteDbModel {
    return NoteDbModel(
        id = id,
        title = title,
        content = content,
        updatedAt = updatedAt,
        isPinned = isPinned
    )
}

fun NoteDbModel.toEntity(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        updatedAt = updatedAt,
        isPinned = isPinned
    )
}