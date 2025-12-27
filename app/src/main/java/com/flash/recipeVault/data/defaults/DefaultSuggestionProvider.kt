package com.flash.recipeVault.data.defaults

import android.content.Context
import androidx.annotation.RawRes
import com.flash.recipeVault.R

object DefaultSuggestionsProvider {

    fun ingredients(context: Context): List<String> =
        readLines(context, R.raw.default_ingredients)

    fun units(context: Context): List<String> =
        readLines(context, R.raw.default_units)

    fun steps(context: Context): List<String> =
        readLines(context, R.raw.default_steps)

    private fun readLines(context: Context, @RawRes resId: Int): List<String> {
        return context.resources
            .openRawResource(resId)
            .bufferedReader()
            .useLines { lines ->
                lines.map { it.trim() }
                    .filter { it.isNotBlank() }
                    .toList()
            }
    }
}