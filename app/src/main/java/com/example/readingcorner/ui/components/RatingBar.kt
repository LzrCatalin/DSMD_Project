package com.example.readingcorner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** A simple 1..5 tappable star rating. */
@Composable
fun RatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Int = 36
) {
    Row(modifier = modifier) {
        for (star in 1..5) {
            val filled = star <= rating
            Icon(
                imageVector = if (filled) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = "Rate $star",
                tint = if (filled) Color(0xFFFFB400) else Color.Gray,
                modifier = Modifier
                    .size(starSize.dp)
                    .clickable { onRatingChange(star.toFloat()) }
            )
        }
    }
}
