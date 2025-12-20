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
//        private val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(db: SupportSQLiteDatabase) {
//                db.execSQL("ALTER TABLE recipes ADD COLUMN imageUri TEXT")
//            }
//        }
//
//
//        private val MIGRATION_2_3 = object : Migration(2, 3) {
//            override fun migrate(db: SupportSQLiteDatabase) {
//                db.execSQL("ALTER TABLE recipes ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
//                db.execSQL("ALTER TABLE recipes ADD COLUMN deletedAt INTEGER")
//            }
//        }
//
//
//        private val MIGRATION_3_4 = object : Migration(3, 4) {
//            override fun migrate(db: SupportSQLiteDatabase) {
//                db.execSQL("ALTER TABLE recipes ADD COLUMN imageBase64 TEXT")
//            }
//        }
//
//        private val MIGRATION_4_5 = object : Migration(4, 5) {
//            override fun migrate(db: SupportSQLiteDatabase) {
//
//                db.execSQL("""
//            CREATE TABLE IF NOT EXISTS recipes_new (
//                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//                title TEXT NOT NULL,
//                description TEXT,
//                imageUri TEXT,
//                imageUrl TEXT,
//                isDeleted INTEGER NOT NULL DEFAULT 0,
//                deletedAt INTEGER,
//                createdAt INTEGER NOT NULL,
//                updatedAt INTEGER NOT NULL
//            )
//        """.trimIndent())
//
//                db.execSQL("""
//            INSERT INTO recipes_new (id, title, description, imageUri, imageUrl, isDeleted, deletedAt, createdAt, updatedAt)
//            SELECT id, title, description, imageUri, NULL, isDeleted, deletedAt, createdAt, updatedAt
//            FROM recipes
//        """.trimIndent())
//
//                db.execSQL("DROP TABLE recipes")
//                db.execSQL("ALTER TABLE recipes_new RENAME TO recipes")
//            }
//        }

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
