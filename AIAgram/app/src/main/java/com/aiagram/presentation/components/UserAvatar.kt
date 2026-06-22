package com.aiagram.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aiagram.presentation.theme.Gold
import com.aiagram.presentation.theme.OnSurfaceMuted
import com.aiagram.presentation.theme.SurfaceVariant

@Composable
fun UserAvatar(
    avatarUrl: String?,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
    showGoldBorder: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (showGoldBorder) Modifier.border(2.dp, Gold, CircleShape)
                else Modifier
            )
            .background(SurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar padrão",
                tint = OnSurfaceMuted,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}
