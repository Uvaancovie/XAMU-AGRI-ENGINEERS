package com.example.xamu_wil_project.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ClientEntity::class, ProjectEntity::class, ProjectDataEntity::class, NoteEntity::class, RouteEntity::class, ImageEntity::class, WeatherSoilEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun projectDao(): ProjectDao
    abstract fun projectDataDao(): ProjectDataDao
    abstract fun noteDao(): NoteDao
    abstract fun routeDao(): RouteDao
    abstract fun imageDao(): ImageDao
    abstract fun weatherSoilDao(): WeatherSoilDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "xamu_local_db")
                    .fallbackToDestructiveMigration() // This will recreate DB on schema changes
                    .build()
                INSTANCE = inst
                inst
            }
        }
    }
}
