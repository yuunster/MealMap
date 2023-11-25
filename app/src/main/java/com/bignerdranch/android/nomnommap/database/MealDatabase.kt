package com.bignerdranch.android.nomnommap.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.nomnommap.Meal

@Database(
    entities = [ Meal::class ],
    version=2,
)
@TypeConverters(MealTypeConverters::class)
abstract class MealDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
}

val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Meal ADD COLUMN photoFileName TEXT"
        )
    }
}