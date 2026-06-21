package com.example.readingcorner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

/** Converts the [ShelfStatus] enum to/from a String column. */
class Converters {
    @TypeConverter
    fun fromStatus(status: ShelfStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): ShelfStatus =
        runCatching { ShelfStatus.valueOf(value) }.getOrDefault(ShelfStatus.TO_READ)
}

@Database(entities = [ShelfBook::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun shelfBookDao(): ShelfBookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reading_corner.db"
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
    }
}
