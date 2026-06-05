package com.mapdir.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapdir.app.ui.theme.StarEmpty
import com.mapdir.app.ui.theme.StarFilled

/**
 * A simple star-based rating display.
 *
 * Renders 5 stars — filled, half, or empty — based on the [rating] value (0.0–5.0).
 */
@Composable
fun RatingBar(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp
) {
    Row(modifier = modifier) {
        for (i in 1..5) {
            val icon = when {
                rating >= i       -> Icons.Filled.Star
                rating >= i - 0.5 -> Icons.Filled.StarHalf
                else              -> Icons.Outlined.StarOutline
            }
            val tint = if (rating >= i - 0.5) StarFilled else StarEmpty

            Icon(
                imageVector = icon,
                contentDescription = "Star $i",
                modifier = Modifier.size(starSize),
                tint = tint
            )
        }
    }
}
