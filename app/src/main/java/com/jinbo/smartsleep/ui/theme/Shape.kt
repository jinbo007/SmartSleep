package com.jinbo.smartsleep.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Shape definitions for consistent corner radius throughout the app
 */
object AppShapes {
    // Small corners (chips, tags)
    val small = RoundedCornerShape(corner = CornerSize(8.dp))

    // Medium corners (buttons, inputs)
    val medium = RoundedCornerShape(corner = CornerSize(12.dp))

    // Large corners (cards)
    val large = RoundedCornerShape(corner = CornerSize(16.dp))

    // Extra large corners (modals, dialogs)
    val xlarge = RoundedCornerShape(corner = CornerSize(24.dp))

    // Full corners (pill shape)
    val full = RoundedCornerShape(50)

    // Top-only rounded corners (for bottom sheets)
    val top = RoundedCornerShape(
        topStart = CornerSize(16.dp),
        topEnd = CornerSize(16.dp),
        bottomStart = CornerSize(0.dp),
        bottomEnd = CornerSize(0.dp)
    )

    // Bottom-only rounded corners
    val bottom = RoundedCornerShape(
        topStart = CornerSize(0.dp),
        topEnd = CornerSize(0.dp),
        bottomStart = CornerSize(16.dp),
        bottomEnd = CornerSize(16.dp)
    )
}
