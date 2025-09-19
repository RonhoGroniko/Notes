package com.sharapov.notes.data.mapper

import com.sharapov.notes.data.db.NoteDbModel
import com.sharapov.notes.domain.entities.Note

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

fun List<NoteDbModel>.toEntities(): List<Note> {
    return this.map { it.toEntity() }
}