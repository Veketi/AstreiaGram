package com.aiagram.presentation.profile.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
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
import com.aiagram.presentation.auth.login.aiagramTextFieldColors
import com.aiagram.presentation.components.AIagramTopBar
import com.aiagram.presentation.components.UserAvatar
import com.aiagram.presentation.theme.*

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onBack()
    }

    Scaffold(
        topBar = {
            AIagramTopBar(
                title = "Editar Perfil",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Gold)
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::save,
                        enabled = !uiState.isLoading
                    ) {
                        Text("Salvar", color = Gold, fontWeight = FontWeight.Bold)
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar preview
            Box(contentAlignment = Alignment.BottomEnd) {
                UserAvatar(
                    avatarUrl = uiState.avatarUrl.ifBlank { null },
                    size = 96.dp,
                    showGoldBorder = true
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Gold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, null, tint = Black, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.avatarUrl,
                onValueChange = viewModel::onAvatarUrlChange,
                label = { Text("URL do Avatar") },
                leadingIcon = { Icon(Icons.Default.Link, null, tint = Gold) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = aiagramTextFieldColors(),
                placeholder = { Text("https://...", color = OnSurfaceMuted) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.bio,
                onValueChange = viewModel::onBioChange,
                label = { Text("Biografia") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                shape = RoundedCornerShape(14.dp),
                colors = aiagramTextFieldColors(),
                maxLines = 4,
                placeholder = { Text("Conta um pouco sobre você...", color = OnSurfaceMuted) }
            )

            AnimatedVisibility(visible = uiState.error != null) {
                Text(uiState.error ?: "", color = Error, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(color = Gold)
            }
        }
    }
}
