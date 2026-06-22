package com.aiagram.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aiagram.presentation.auth.login.LoginScreen
import com.aiagram.presentation.auth.register.RegisterScreen
import com.aiagram.presentation.auth.splash.SplashScreen
import com.aiagram.presentation.feed.FeedScreen
import com.aiagram.presentation.post.create.CreatePostScreen
import com.aiagram.presentation.post.detail.PostDetailScreen
import com.aiagram.presentation.profile.edit.EditProfileScreen
import com.aiagram.presentation.profile.followlist.FollowListScreen
import com.aiagram.presentation.profile.me.ProfileScreen
import com.aiagram.presentation.profile.user.UserProfileScreen
import com.aiagram.presentation.search.SearchScreen

@Composable
fun AIagramNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToFeed = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Feed.route) {
            FeedScreen(
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToPostDetail = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                },
                onNavigateToCreatePost = { navController.navigate(Screen.CreatePost.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                }
            )
        }

        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                onPostCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            PostDetailScreen(
                postId = postId,
                onBack = { navController.popBackStack() },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToFollowers = { userId ->
                    navController.navigate(Screen.FollowList.createRoute(userId, "followers"))
                },
                onNavigateToFollowing = { userId ->
                    navController.navigate(Screen.FollowList.createRoute(userId, "following"))
                },
                onNavigateToPostDetail = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            UserProfileScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
                onNavigateToFollowers = {
                    navController.navigate(Screen.FollowList.createRoute(userId, "followers"))
                },
                onNavigateToFollowing = {
                    navController.navigate(Screen.FollowList.createRoute(userId, "following"))
                },
                onNavigateToPostDetail = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                }
            )
        }

        composable(
            route = Screen.FollowList.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val type = backStackEntry.arguments?.getString("type") ?: "followers"
            FollowListScreen(
                userId = userId,
                type = type,
                onBack = { navController.popBackStack() },
                onNavigateToUserProfile = { uid ->
                    navController.navigate(Screen.UserProfile.createRoute(uid))
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                }
            )
        }
    }
}
