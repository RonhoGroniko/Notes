package com.sharapov.notes.data.mapper

import com.sharapov.notes.data.db.models.ContentItemDbModel
import com.sharapov.notes.data.db.models.ContentType
import com.sharapov.notes.data.db.models.NoteDbModel
import com.sharapov.notes.data.db.models.NoteWithContentDbModel
import com.sharapov.notes.domain.entities.ContentItem
import com.sharapov.notes.domain.entities.Note
import kotlinx.serialization.json.Json

fun Note.toDbModel(): NoteDbModel {
    return NoteDbModel(
        id = id,
        title = title,
        updatedAt = updatedAt,
        isPinned = isPinned
    )
}

fun NoteWithContentDbModel.toEntity(): Note {
    return Note(
        id = noteDbModel.id,
        title = noteDbModel.title,
        content = content.toContentItemEntities(),
        updatedAt = noteDbModel.updatedAt,
        isPinned = noteDbModel.isPinned
    )
}

fun List<NoteWithContentDbModel>.toEntities(): List<Note> {
    return this.map { it.toEntity() }
}

fun List<ContentItem>.toContentItemDbModels(noteId: Int): List<ContentItemDbModel> {
    return this.mapIndexed { index, contentItem ->
        when(contentItem) {
            is ContentItem.Image -> {
                ContentItemDbModel(
                    noteId = noteId,
                    contentType = ContentType.IMAGE,
                    content = contentItem.url,
                    order = index
                )
            }
            is ContentItem.Text -> {
                ContentItemDbModel(
                    noteId = noteId,
                    contentType = ContentType.TEXT,
                    content = contentItem.content,
                    order = index
                )
            }
        }
    }
}


fun List<ContentItemDbModel>.toContentItemEntities(): List<ContentItem> {
    return this.map { contentItemDbModel ->
        when(contentItemDbModel.contentType) {
            ContentType.TEXT -> {
                ContentItem.Text(content = contentItemDbModel.content)
            }
            ContentType.IMAGE -> {
                ContentItem.Image(url = contentItemDbModel.content)
            }
        }
    }
}