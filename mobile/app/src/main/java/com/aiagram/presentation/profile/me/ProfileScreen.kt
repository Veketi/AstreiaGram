package com.aiagram.presentation.profile.me

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToFollowers: (String) -> Unit,
    onNavigateToFollowing: (String) -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            AIagramTopBar(
                title = uiState.user?.username ?: "Perfil",
                actions = {
                    IconButton(onClick = onNavigateToEditProfile) {
                        Icon(Icons.Default.Edit, "Editar perfil", tint = Gold)
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, "Sair", tint = OnSurfaceMuted)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val user = uiState.user
                if (user != null) {
                    // Profile header
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
                                StatColumn(label = "Posts", count = uiState.posts.size.toString())
                                Box(Modifier.clickable { onNavigateToFollowers(user.id) }) {
                                    StatColumn(label = "Seguidores", count = user.followerCount.toString())
                                }
                                Box(Modifier.clickable { onNavigateToFollowing(user.id) }) {
                                    StatColumn(label = "Seguindo", count = user.followingCount.toString())
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(user.username, fontWeight = FontWeight.Bold, color = OnBackground, fontSize = 15.sp)
                        if (!user.bio.isNullOrBlank()) {
                            Text(user.bio, color = OnSurface, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onNavigateToEditProfile,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OnBackground)
                        ) {
                            Text("Editar Perfil", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    HorizontalDivider(color = Divider)

                    // Posts grid
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

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sair da conta?", color = OnBackground) },
            confirmButton = {
                TextButton(onClick = { viewModel.logout(onLogout) }) {
                    Text("Sair", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = OnSurfaceMuted)
                }
            },
            containerColor = SurfaceVariant
        )
    }
}

@Composable
private fun StatColumn(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnBackground)
        Text(label, fontSize = 12.sp, color = OnSurfaceMuted)
    }
}
