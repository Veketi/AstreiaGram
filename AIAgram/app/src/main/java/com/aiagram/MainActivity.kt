package com.aiagram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aiagram.navigation.AIagramNavGraph
import com.aiagram.presentation.theme.AIagramTheme
import com.aiagram.presentation.theme.Background
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIagramTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Background
                ) {
                    val navController = rememberNavController()
                    androidx.compose.material3.Scaffold(
                        bottomBar = { com.aiagram.presentation.components.AIagramBottomBar(navController) },
                        content = { padding ->
                            androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) {
                                AIagramNavGraph(navController = navController)
                            }
                        }
                    )
                }
            }
        }
    }
}
