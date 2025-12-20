package com.flash.recipeVault.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter

@Composable
fun RecipeAsyncImage(
    model: Any?,
    contentScale: ContentScale = ContentScale.Crop,
) {
    AsyncImage(
        model = model,
        contentDescription = "Recipe Image",
        modifier = Modifier
            .width(100.dp)
            .height(100.dp),
        contentScale = contentScale,
    )
}

@Composable
fun RecipeImage(
    model: Any?,
    contentScale: ContentScale = ContentScale.Crop,
) {
    Image(
        painter = rememberAsyncImagePainter(model),
        contentScale = contentScale,
        contentDescription = "Recipe image",
        modifier = Modifier
            .height(100.dp)
            .width(100.dp)
    )
}