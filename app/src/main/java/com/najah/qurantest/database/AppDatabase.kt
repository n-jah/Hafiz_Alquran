package com.najah.qurantest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.najah.qurantest.model.QuranVerse
@Database(entities = [QuranVerse::class], version = 1)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao

    companion object {
        @Volatile
        private var INSTANCE: QuranDatabase? = null

        fun getInstance(context: Context): QuranDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuranDatabase::class.java,
                    "quran_database"
                ).createFromAsset("qurandb.db").
                build()
                INSTANCE = instance
                instance
            }
        }
    }
}
