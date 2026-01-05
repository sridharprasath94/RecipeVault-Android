package com.flash.recipeVault.ui.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.flash.recipeVault.R

@Composable
fun RecipeAsyncImage(
    model: Any?,
    modifier: Modifier = Modifier,
    width: Dp = 100.dp,
    height: Dp = 100.dp,
    cornerRadius: Dp = 10.dp,
    contentScale: ContentScale = ContentScale.Crop,
    onImageClick: (() -> Unit)? = null,
) {
    val isPreview = LocalInspectionMode.current

    val imageModifier = modifier
        .width(width)
        .height(height)
        .clip(RoundedCornerShape(cornerRadius))
        .let {
            if (onImageClick != null) it.clickable { onImageClick() }
            else it
        }

    if (isPreview) {
        Image(
            painter = painterResource(R.drawable.preview_food),
            contentDescription = "Recipe Image",
            modifier = imageModifier,
            contentScale = contentScale,
        )
    } else {
        AsyncImage(
            model = model,
            contentDescription = "Recipe Image",
            modifier = imageModifier,
            contentScale = contentScale,
        )
    }
}

@Composable
fun RecipeImage(
    model: Any?,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        Image(
            painter = painterResource(R.drawable.preview_food),
            contentDescription = "Recipe Image",
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = contentScale,
        )
    } else {
        Image(
            painter = rememberAsyncImagePainter(model),
            contentScale = contentScale,
            contentDescription = "Recipe image",
            modifier = Modifier
                .height(100.dp)
                .width(100.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
    }

}