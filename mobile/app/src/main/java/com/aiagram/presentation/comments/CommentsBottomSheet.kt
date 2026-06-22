package com.aiagram.presentation.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiagram.domain.model.Comment
import com.aiagram.presentation.auth.login.aiagramTextFieldColors
import com.aiagram.presentation.components.UserAvatar
import com.aiagram.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    postId: String,
    onDismiss: () -> Unit,
    viewModel: CommentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(postId) { viewModel.loadComments(postId) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(40.dp, 4.dp)
                    .background(Divider, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Comentários",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = OnBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 400.dp)
                    .padding(horizontal = 16.dp)
            ) {
                if (uiState.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Gold, modifier = Modifier.size(28.dp))
                        }
                    }
                } else if (uiState.comments.isEmpty()) {
                    item {
                        Text(
                            "Nenhum comentário ainda. Seja o primeiro!",
                            color = OnSurfaceMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                } else {
                    items(uiState.comments, key = { it.id }) { comment ->
                        val author = uiState.authors[comment.userId]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                UserAvatar(
                                    avatarUrl = author?.avatarUrl,
                                    size = 32.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = author?.username ?: comment.userId.take(8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Gold
                                    )
                                    Text(
                                        text = comment.content,
                                        fontSize = 14.sp,
                                        color = OnBackground
                                    )
                                }
                            }
                            if (comment.userId == uiState.currentUserId) {
                                IconButton(
                                    onClick = { viewModel.deleteComment(postId, comment.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, "Deletar", tint = Error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        HorizontalDivider(color = Divider)
                    }
                }
            }

            // Comment input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.newComment,
                    onValueChange = viewModel::onCommentChange,
                    placeholder = { Text("Adicione um comentário...", color = OnSurfaceMuted, fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = aiagramTextFieldColors()
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.addComment(postId) },
                    enabled = !uiState.isSending && uiState.newComment.isNotBlank()
                ) {
                    if (uiState.isSending) {
                        CircularProgressIndicator(color = Gold, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Send, "Enviar", tint = if (uiState.newComment.isNotBlank()) Gold else OnSurfaceMuted)
                    }
                }
            }
        }
    }
}
