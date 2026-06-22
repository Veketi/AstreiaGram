package com.aiagram.presentation.auth.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiagram.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiagram.presentation.theme.Background
import com.aiagram.presentation.theme.Gold
import com.aiagram.presentation.theme.OnSurfaceMuted

@Composable
fun SplashScreen(
    onNavigateToFeed: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val scaleAnim = remember { Animatable(0.6f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 120f))
        alphaAnim.animateTo(1f, animationSpec = tween(400))
    }

    LaunchedEffect(state) {
        when (state) {
            is SplashState.Authenticated -> onNavigateToFeed()
            is SplashState.Unauthenticated -> onNavigateToLogin()
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scaleAnim.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "AstreiaGram",
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = Gold,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "conecte. compartilhe. inspire.",
                fontSize = 12.sp,
                color = OnSurfaceMuted.copy(alpha = alphaAnim.value),
                letterSpacing = 2.sp
            )
        }
    }
}
