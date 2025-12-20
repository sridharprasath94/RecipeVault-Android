package com.flash.recipeVault.util

import com.flash.recipeVault.data.IngredientEntity
import com.flash.recipeVault.data.RecipeEntity
import com.flash.recipeVault.data.StepEntity
import org.json.JSONArray

data class ImportBundle(
    val recipes: List<RecipeEntity>,
    val ingredients: List<IngredientEntity>,
    val steps: List<StepEntity>
)

object JsonImportParser {
    fun parse(json: String): ImportBundle {
        val arr = JSONArray(json)

        val recipes = mutableListOf<RecipeEntity>()
        val ingredients = mutableListOf<IngredientEntity>()
        val steps = mutableListOf<StepEntity>()

        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)
            val r = item.getJSONObject("recipe")

            val id = r.getLong("id")
            val title = r.getString("title")
            val description = if (r.isNull("description")) null else r.getString("description")
            val createdAt = r.optLong("createdAt", System.currentTimeMillis())
            val updatedAt = r.optLong("updatedAt", createdAt)
            val imageUri = if (r.has("imageUri") && !r.isNull("imageUri")) r.getString("imageUri") else null
            val imageUrl = if (r.has("imageUrl") && !r.isNull("imageUrl")) r.getString("imageUrl") else null

            recipes.add(
                RecipeEntity(
                    id = id,
                    title = title,
                    description = description,
                    imageUri = imageUri,
                    imageUrl = imageUrl,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            )

            val ingArr = item.getJSONArray("ingredients")
            for (j in 0 until ingArr.length()) {
                val ing = ingArr.getJSONObject(j)
                ingredients.add(
                    IngredientEntity(
                        recipeId = id,
                        name = ing.getString("name"),
                        quantity = if (ing.isNull("quantity")) null else ing.getString("quantity"),
                        unit = if (ing.isNull("unit")) null else ing.getString("unit"),
                        sortOrder = ing.optInt("sortOrder", j)
                    )
                )
            }

            val stArr = item.getJSONArray("steps")
            for (j in 0 until stArr.length()) {
                val st = stArr.getJSONObject(j)
                steps.add(
                    StepEntity(
                        recipeId = id,
                        instruction = st.getString("instruction"),
                        sortOrder = st.optInt("sortOrder", j)
                    )
                )
            }
        }

        return ImportBundle(recipes, ingredients, steps)
    }
}
