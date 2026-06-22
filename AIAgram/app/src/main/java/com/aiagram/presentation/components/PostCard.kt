package com.aiagram.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aiagram.domain.model.Post
import com.aiagram.presentation.theme.*

@Composable
fun PostCard(
    post: Post,
    authorUsername: String = "",
    authorAvatarUrl: String? = null,
    isLiked: Boolean = false,
    onLikeToggle: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onUserClick: () -> Unit = {},
    onPostClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var likeAnimating by remember { mutableStateOf(false) }
    val likeScale by animateFloatAsState(
        targetValue = if (likeAnimating) 1.35f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 200f),
        finishedListener = { likeAnimating = false },
        label = "likeScale"
    )
    val heartColor by animateColorAsState(
        targetValue = if (isLiked) Gold else OnSurfaceMuted,
        label = "heartColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onUserClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                avatarUrl = authorAvatarUrl,
                size = 36.dp,
                showGoldBorder = true
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = authorUsername.ifBlank { post.userId.take(8) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground
            )
        }

        // Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable(onClick = onPostClick)
        ) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = post.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Actions row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like button
            IconButton(
                onClick = {
                    likeAnimating = true
                    onLikeToggle()
                }
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Curtir",
                    tint = heartColor,
                    modifier = Modifier.scale(likeScale)
                )
            }
            Text(
                text = "${post.likeCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceMuted
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Comment button
            IconButton(onClick = onCommentClick) {
                Icon(
                    imageVector = Icons.Default.ModeComment,
                    contentDescription = "Comentar",
                    tint = OnSurfaceMuted
                )
            }
            Text(
                text = "${post.commentCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceMuted
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        // Caption
        if (!post.caption.isNullOrBlank()) {
            Row(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 8.dp)) {
                Text(
                    text = authorUsername.ifBlank { post.userId.take(8) } + " ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gold
                )
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface
                )
            }
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Divider)
        )
    }
}
