package com.jinbo.smartsleep.ui.components.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

/**
 * AnimatedCounter - Animates number changes with slide animation
 *
 * @param targetValue The target number to display
 * @param modifier Modifier
 * @param style Text style for the counter
 * @param animationDurationMs Duration of the animation in milliseconds
 */
@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayMedium,
    animationDurationMs: Int = 500
) {
    AnimatedContent(
        targetState = targetValue,
        transitionSpec = {
            // Slide up from bottom for increasing numbers
            // Slide down from top for decreasing numbers
            val direction = if (targetState > initialState) 1 else -1

            slideInVertically(
                animationSpec = tween(animationDurationMs),
                initialOffsetY = { it * direction / 2 }
            ) + fadeIn(
                animationSpec = tween(animationDurationMs)
            ) togetherWith slideOutVertically(
                animationSpec = tween(animationDurationMs),
                targetOffsetY = { -it * direction / 2 }
            ) + fadeOut(
                animationSpec = tween(animationDurationMs)
            )
        },
        label = "counter_animation",
        modifier = modifier
    ) { targetCount ->
        Text(
            text = targetCount.toString(),
            style = style
        )
    }
}
