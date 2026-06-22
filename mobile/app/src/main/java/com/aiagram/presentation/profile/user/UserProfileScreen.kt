package com.aiagram.presentation.profile.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.aiagram.presentation.components.AIagramTopBar
import com.aiagram.presentation.components.UserAvatar
import com.aiagram.presentation.theme.*

@Composable
fun UserProfileScreen(
    userId: String,
    onBack: () -> Unit,
    onNavigateToFollowers: () -> Unit,
    onNavigateToFollowing: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) { viewModel.loadUser(userId) }

    Scaffold(
        topBar = {
            AIagramTopBar(
                title = uiState.user?.username ?: "Perfil",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Gold)
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
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                val user = uiState.user
                if (user != null) {
                    // Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(avatarUrl = user.avatarUrl, size = 80.dp, showGoldBorder = true)
                            Spacer(modifier = Modifier.width(24.dp))
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${uiState.posts.size}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnBackground)
                                    Text("Posts", fontSize = 12.sp, color = OnSurfaceMuted)
                                }
                                Box(Modifier.clickable(onClick = onNavigateToFollowers)) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${user.followerCount}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnBackground)
                                        Text("Seguidores", fontSize = 12.sp, color = OnSurfaceMuted)
                                    }
                                }
                                Box(Modifier.clickable(onClick = onNavigateToFollowing)) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${user.followingCount}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnBackground)
                                        Text("Seguindo", fontSize = 12.sp, color = OnSurfaceMuted)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(user.username, fontWeight = FontWeight.Bold, color = OnBackground, fontSize = 15.sp)
                        if (!user.bio.isNullOrBlank()) {
                            Text(user.bio, color = OnSurface, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Follow button — only show if not viewing own profile
                        if (userId != uiState.currentUserId) {
                            Button(
                                onClick = { viewModel.toggleFollow(userId) },
                                enabled = !uiState.isFollowLoading,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.isFollowing) SurfaceVariant else Gold,
                                    contentColor = if (uiState.isFollowing) OnBackground else Black
                                )
                            ) {
                                if (uiState.isFollowLoading) {
                                    CircularProgressIndicator(
                                        color = if (uiState.isFollowing) Gold else Black,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        if (uiState.isFollowing) "Seguindo" else "Seguir",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Divider)

                    Row(
                        modifier = Modifier.fillMaxWidth().background(Surface).padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.GridOn, null, tint = Gold)
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(1.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        items(uiState.posts, key = { it.id }) { post ->
                            AsyncImage(
                                model = post.imageUrl,
                                contentDescription = post.caption,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clickable { onNavigateToPostDetail(post.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
