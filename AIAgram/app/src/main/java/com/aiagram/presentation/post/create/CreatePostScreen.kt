package com.aiagram.presentation.post.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.aiagram.presentation.auth.login.aiagramTextFieldColors
import com.aiagram.presentation.components.AIagramTopBar
import com.aiagram.presentation.theme.*

@Composable
fun CreatePostScreen(
    onPostCreated: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onPostCreated()
    }

    Scaffold(
        topBar = {
            AIagramTopBar(
                title = "Novo Post",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Gold)
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::createPost,
                        enabled = !uiState.isLoading
                    ) {
                        Text("Publicar", color = Gold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceVariant)
                    .border(1.dp, if (uiState.imageUrl.isNotBlank()) Gold else Divider, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = uiState.imageUrl,
                        contentDescription = "Preview da imagem",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = OnSurfaceMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Preview da imagem", color = OnSurfaceMuted, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Image URL field
            OutlinedTextField(
                value = uiState.imageUrl,
                onValueChange = viewModel::onImageUrlChange,
                label = { Text("URL da Imagem") },
                leadingIcon = { Icon(Icons.Default.Link, null, tint = Gold) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = aiagramTextFieldColors(),
                placeholder = { Text("https://...", color = OnSurfaceMuted) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Caption field
            OutlinedTextField(
                value = uiState.caption,
                onValueChange = viewModel::onCaptionChange,
                label = { Text("Legenda") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                shape = RoundedCornerShape(14.dp),
                colors = aiagramTextFieldColors(),
                maxLines = 5,
                placeholder = { Text("Escreva uma legenda...", color = OnSurfaceMuted) }
            )

            AnimatedVisibility(visible = uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = Error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(color = Gold)
            }
        }
    }
}
