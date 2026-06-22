package com.aiagram.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiagram.presentation.auth.login.aiagramTextFieldColors
import com.aiagram.presentation.components.AIagramTopBar
import com.aiagram.presentation.components.UserAvatar
import com.aiagram.presentation.theme.*

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            AIagramTopBar(
                title = "Buscar",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Gold)
                    }
                }
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Username ou ID") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Gold) },
                trailingIcon = {
                    if (uiState.query.isNotBlank()) {
                        TextButton(onClick = {
                            keyboardController?.hide()
                            viewModel.search()
                        }) {
                            Text("Buscar", color = Gold, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = aiagramTextFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        viewModel.search()
                    }
                ),
                placeholder = { Text("Digite o username ou cole o UUID", color = OnSurfaceMuted) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Loading
            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold)
                }
            }

            // Not found
            AnimatedVisibility(visible = uiState.notFound) {
                Box(Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                    Text("Usuário não encontrado", color = OnSurfaceMuted, fontSize = 14.sp)
                }
            }

            // Result card
            uiState.result?.let { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToUserProfile(user.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(avatarUrl = user.avatarUrl, size = 56.dp, showGoldBorder = true)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(user.username, fontWeight = FontWeight.Bold, color = Gold, fontSize = 16.sp)
                            if (!user.bio.isNullOrBlank()) {
                                Text(user.bio, color = OnSurface, fontSize = 13.sp, maxLines = 2)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                Text("${user.followerCount} seguidores", color = OnSurfaceMuted, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("${user.followingCount} seguindo", color = OnSurfaceMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Hint text
            if (uiState.result == null && !uiState.isLoading && !uiState.notFound) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, null, tint = OnSurfaceMuted.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Busque um usuário pelo username ou ID", color = OnSurfaceMuted, fontSize = 14.sp)
                        Text("(Ex: joaosilva ou xxxxxxxx-xxxx-...)", color = OnSurfaceMuted.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
