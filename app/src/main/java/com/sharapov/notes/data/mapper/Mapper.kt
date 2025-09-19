package com.sharapov.notes.data.mapper

import com.sharapov.notes.data.db.models.ContentItemDbModel
import com.sharapov.notes.data.db.models.NoteDbModel
import com.sharapov.notes.domain.entities.ContentItem
import com.sharapov.notes.domain.entities.Note
import kotlinx.serialization.json.Json

fun Note.toDbModel(): NoteDbModel {
    val contentAsString = Json.encodeToString(content.toContentItemDbModels())
    return NoteDbModel(
        id = id,
        title = title,
        content = contentAsString,
        updatedAt = updatedAt,
        isPinned = isPinned
    )
}

fun NoteDbModel.toEntity(): Note {
    val contentItemDbModels = Json.decodeFromString<List<ContentItemDbModel>>(content)
    return Note(
        id = id,
        title = title,
        content = contentItemDbModels.toContentItemEntities(),
        updatedAt = updatedAt,
        isPinned = isPinned
    )
}

fun List<NoteDbModel>.toEntities(): List<Note> {
    return this.map { it.toEntity() }
}

fun List<ContentItem>.toContentItemDbModels(): List<ContentItemDbModel> {
    return this.map { contentItem ->
        when(contentItem) {
            is ContentItem.Image -> {
                ContentItemDbModel.Image(url = contentItem.url)
            }
            is ContentItem.Text -> {
                ContentItemDbModel.Text(content = contentItem.content)
            }
        }
    }
}


fun List<ContentItemDbModel>.toContentItemEntities(): List<ContentItem> {
    return this.map { contentItemDbModel ->
        when(contentItemDbModel) {
            is ContentItemDbModel.Image -> {
                ContentItem.Image(url = contentItemDbModel.url)
            }
            is ContentItemDbModel.Text -> {
                ContentItem.Text(content = contentItemDbModel.content)
            }
        }
    }
}