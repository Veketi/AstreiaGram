package com.aiagram.presentation.post.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.aiagram.presentation.comments.CommentsBottomSheet
import com.aiagram.presentation.components.AIagramTopBar
import com.aiagram.presentation.components.UserAvatar
import com.aiagram.presentation.theme.*

@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showComments by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var likeAnimating by remember { mutableStateOf(false) }
    val likeScale by animateFloatAsState(
        targetValue = if (likeAnimating) 1.4f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 200f),
        finishedListener = { likeAnimating = false },
        label = "likeScale"
    )
    val heartColor by animateColorAsState(
        targetValue = if (uiState.isLiked) Gold else OnSurfaceMuted,
        label = "heartColor"
    )

    LaunchedEffect(postId) { viewModel.loadPost(postId) }

    Scaffold(
        topBar = {
            AIagramTopBar(
                title = "Post",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Gold)
                    }
                },
                actions = {
                    val post = uiState.post
                    val isOwner = post?.userId == uiState.currentUserId
                    if (isOwner && post != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Deletar", tint = Error)
                        }
                    }
                }
            )
        },
        containerColor = Background
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
        } else {
            val post = uiState.post
            if (post != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Author row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToUserProfile(post.userId) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            avatarUrl = uiState.author?.avatarUrl,
                            size = 42.dp,
                            showGoldBorder = true
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = uiState.author?.username ?: post.userId.take(8),
                                fontWeight = FontWeight.Bold,
                                color = OnBackground,
                                fontSize = 15.sp
                            )
                            uiState.author?.bio?.let {
                                Text(text = it, color = OnSurfaceMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    // Image
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = post.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            likeAnimating = true
                            viewModel.toggleLike()
                        }) {
                            Icon(
                                imageVector = if (uiState.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Curtir",
                                tint = heartColor,
                                modifier = Modifier.scale(likeScale)
                            )
                        }
                        Text("${post.likeCount} curtidas", color = OnSurfaceMuted, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = { showComments = true }) {
                            Icon(Icons.Default.ModeComment, "Comentários", tint = OnSurfaceMuted)
                        }
                        Text("${post.commentCount} comentários", color = OnSurfaceMuted, fontSize = 13.sp)
                    }

                    // Caption
                    if (!post.caption.isNullOrBlank()) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                            Text(
                                text = (uiState.author?.username ?: post.userId.take(8)) + " ",
                                fontWeight = FontWeight.Bold,
                                color = Gold,
                                fontSize = 14.sp
                            )
                            Text(post.caption, color = OnSurface, fontSize = 14.sp)
                        }
                    }

                    // Date
                    if (!post.createdAt.isNullOrBlank()) {
                        Text(
                            text = post.createdAt.take(10),
                            color = OnSurfaceMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Deletar post?", color = OnBackground) },
                text = { Text("Esta ação não pode ser desfeita.", color = OnSurfaceMuted) },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        viewModel.deletePost(postId) { onBack() }
                    }) { Text("Deletar", color = Error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar", color = OnSurfaceMuted)
                    }
                },
                containerColor = SurfaceVariant
            )
        }

        if (showComments) {
            CommentsBottomSheet(
                postId = postId,
                onDismiss = { showComments = false }
            )
        }
    }
}
