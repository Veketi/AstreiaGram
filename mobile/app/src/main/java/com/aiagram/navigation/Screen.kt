package com.aiagram.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Feed : Screen("feed")
    object CreatePost : Screen("create_post")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Search : Screen("search")

    object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(postId: String) = "post_detail/$postId"
    }

    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }

    object FollowList : Screen("follow_list/{userId}/{type}") {
        fun createRoute(userId: String, type: String) = "follow_list/$userId/$type"
    }
}
