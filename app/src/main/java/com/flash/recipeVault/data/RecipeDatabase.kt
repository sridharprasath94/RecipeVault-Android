package com.flash.recipeVault.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RecipeEntity::class, IngredientEntity::class, StepEntity::class],
    version = 5,
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null


        @Volatile
        private var LAST_NAME: String? = null

        /**
         * Default DB (fallback). In this app we use per-user databases named recipe_db_<uid>.
         */
        fun get(context: Context): RecipeDatabase = get(context, "recipe_db")

        fun get(context: Context, dbName: String): RecipeDatabase {
            val existing = INSTANCE
            if (existing != null && LAST_NAME == dbName) return existing

            return synchronized(this) {
                val current = INSTANCE
                if (current != null && LAST_NAME == dbName) return@synchronized current

                Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    dbName
                )
                    .build()
                    .also {
                        INSTANCE = it
                        LAST_NAME = dbName
                    }
            }
        }
    }
}
