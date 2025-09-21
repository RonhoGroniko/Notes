package com.sharapov.notes.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sharapov.notes.data.db.models.ContentItemDbModel
import com.sharapov.notes.data.db.models.NoteDbModel

@Database(
    entities = [NoteDbModel::class, ContentItemDbModel::class],
    version = 3,
    exportSchema = false
)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun notesDao(): NotesDao


    companion object {

        private val LOCK = Any()

        private var instance: NotesDatabase? = null

        fun getInstance(context: Context): NotesDatabase {
            instance?.let { return it }
            synchronized(LOCK) {
                instance?.let { return it }
                return Room.databaseBuilder(
                    context = context,
                    klass = NotesDatabase::class.java,
                    "notes.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also {
                        instance = it
                    }
            }
        }
    }
}