package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CalculationHistoryEntity::class], version = 1, exportSchema = false)
abstract class OmniCalcDatabase : RoomDatabase() {
    abstract fun calculationHistoryDao(): CalculationHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: OmniCalcDatabase? = null

        fun getDatabase(context: Context): OmniCalcDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OmniCalcDatabase::class.java,
                    "omnicalc_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
