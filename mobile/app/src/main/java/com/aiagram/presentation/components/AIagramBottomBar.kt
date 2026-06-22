package com.aiagram.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aiagram.navigation.Screen
import com.aiagram.presentation.theme.Gold
import com.aiagram.presentation.theme.OnSurfaceMuted
import com.aiagram.presentation.theme.Surface
import com.aiagram.presentation.theme.SurfaceVariant

@Composable
fun AIagramBottomBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val screensWithBottomBar = listOf(
        Screen.Feed.route,
        Screen.Search.route,
        Screen.CreatePost.route,
        Screen.Profile.route,
        Screen.UserProfile.route,
        Screen.FollowList.route
    )

    // Check if current route matches or starts with one of the allowed screens
    val showBottomBar = screensWithBottomBar.any { currentRoute?.startsWith(it.substringBefore("/{")) == true }

    if (showBottomBar) {
        NavigationBar(
            containerColor = Surface,
            contentColor = Gold,
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                selected = currentRoute == Screen.Feed.route,
                onClick = {
                    if (currentRoute != Screen.Feed.route) {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Feed.route) { inclusive = false }
                        }
                    }
                },
                icon = { Icon(Icons.Default.Home, "Feed") },
                label = { Text("Feed") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Gold,
                    selectedTextColor = Gold,
                    indicatorColor = SurfaceVariant,
                    unselectedIconColor = OnSurfaceMuted
                )
            )
            NavigationBarItem(
                selected = currentRoute == Screen.Search.route,
                onClick = {
                    if (currentRoute != Screen.Search.route) {
                        navController.navigate(Screen.Search.route)
                    }
                },
                icon = { Icon(Icons.Default.Search, "Buscar") },
                label = { Text("Buscar") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Gold,
                    selectedTextColor = Gold,
                    indicatorColor = SurfaceVariant,
                    unselectedIconColor = OnSurfaceMuted
                )
            )
            NavigationBarItem(
                selected = currentRoute == Screen.CreatePost.route,
                onClick = {
                    if (currentRoute != Screen.CreatePost.route) {
                        navController.navigate(Screen.CreatePost.route)
                    }
                },
                icon = { Icon(Icons.Default.AddCircleOutline, "Criar") },
                label = { Text("Criar") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Gold,
                    selectedTextColor = Gold,
                    indicatorColor = SurfaceVariant,
                    unselectedIconColor = OnSurfaceMuted
                )
            )
            NavigationBarItem(
                selected = currentRoute == Screen.Profile.route,
                onClick = {
                    if (currentRoute != Screen.Profile.route) {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Feed.route) { inclusive = false }
                        }
                    }
                },
                icon = { Icon(Icons.Default.Person, "Perfil") },
                label = { Text("Perfil") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Gold,
                    selectedTextColor = Gold,
                    indicatorColor = SurfaceVariant,
                    unselectedIconColor = OnSurfaceMuted
                )
            )
        }
    }
}
