package com.aiagram.presentation.profile.followlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiagram.presentation.components.AIagramTopBar
import com.aiagram.presentation.components.UserAvatar
import com.aiagram.presentation.theme.*

@Composable
fun FollowListScreen(
    userId: String,
    type: String,
    onBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    viewModel: FollowListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val title = if (type == "followers") "Seguidores" else "Seguindo"

    LaunchedEffect(userId, type) { viewModel.load(userId, type) }

    Scaffold(
        topBar = {
            AIagramTopBar(
                title = title,
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
        } else if (uiState.users.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = if (type == "followers") "Nenhum seguidor ainda" else "Não está seguindo ninguém",
                    color = OnSurfaceMuted, fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.users, key = { it.id }) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToUserProfile(user.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(avatarUrl = user.avatarUrl, size = 46.dp, showGoldBorder = true)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = user.username,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground,
                            fontSize = 15.sp
                        )
                    }
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}
