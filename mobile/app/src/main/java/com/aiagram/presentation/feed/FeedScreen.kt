package com.aiagram.presentation.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiagram.presentation.comments.CommentsBottomSheet
import com.aiagram.presentation.components.AIagramTopBar
import com.aiagram.presentation.components.PostCard
import com.aiagram.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var commentsPostId by remember { mutableStateOf<String?>(null) }

    // Infinite scroll: load more when near end
    LaunchedEffect(listState.firstVisibleItemIndex, listState.layoutInfo.totalItemsCount) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val total = listState.layoutInfo.totalItemsCount
        if (total > 0 && lastVisible >= total - 3) {
            viewModel.loadFeed()
        }
    }

    // Refresh when returning to this screen
    LaunchedEffect(Unit) {
        // Silently fetch to update feed without losing current scroll position if possible
        // Actually since loadFeed(refresh=true) resets to page 1, we just do it.
        viewModel.loadFeed(refresh = true)
    }

    Scaffold(
        topBar = {
            AIagramTopBar(title = "AstreiaGram")
        },
        containerColor = Background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.loadFeed(refresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.posts.isEmpty() && !uiState.isLoading && !uiState.isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Seu feed está vazio", color = OnSurfaceMuted, fontSize = 16.sp)
                        Text("Siga pessoas para ver posts aqui!", color = OnSurfaceMuted, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().background(Background)
                ) {
                    items(uiState.posts, key = { it.id }) { post ->
                        val author = uiState.authorCache[post.userId]
                        PostCard(
                            post = post,
                            authorUsername = author?.username ?: "",
                            authorAvatarUrl = author?.avatarUrl,
                            isLiked = uiState.likedPostIds.contains(post.id),
                            onLikeToggle = { viewModel.toggleLike(post) },
                            onCommentClick = { commentsPostId = post.id },
                            onUserClick = { onNavigateToUserProfile(post.userId) },
                            onPostClick = { onNavigateToPostDetail(post.id) }
                        )
                    }
                    if (uiState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Gold, strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }

        // Comments sheet
        commentsPostId?.let { postId ->
            CommentsBottomSheet(
                postId = postId,
                onDismiss = { commentsPostId = null }
            )
        }
    }
}
