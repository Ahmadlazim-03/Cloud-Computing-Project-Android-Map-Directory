package com.mapdir.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.unit.dp
import com.mapdir.app.data.repository.PlaceRepository
import com.mapdir.app.ui.detail.DetailScreen
import com.mapdir.app.ui.home.HomeScreen
import com.mapdir.app.ui.list.PlaceListScreen
import com.mapdir.app.ui.map.MapScreen

/**
 * Navigation routes definitions.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object MapPlaceholder : Screen("map_placeholder")
    data object PlaceList : Screen("places?categorySlug={categorySlug}&categoryName={categoryName}") {
        fun createRoute(categorySlug: String?, categoryName: String): String {
            return "places?categorySlug=${categorySlug ?: ""}&categoryName=$categoryName"
        }
    }
    data object Detail : Screen("detail/{placeId}") {
        fun createRoute(placeId: Int): String {
            return "detail/$placeId"
        }
    }
}

/**
 * Main application navigation component.
 *
 * Implements single Activity navigation with a Persistent Bottom Bar
 * for the two main tabs: "Beranda" (Home) and "Peta" (Map).
 *
 * Handles deep navigation to [PlaceListScreen] and [DetailScreen] properly.
 */
@Composable
fun AppNavigation(
    onOpenRoute: (latitude: Double, longitude: Double) -> Unit,
    placeRepository: PlaceRepository
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if bottom bar should be visible (only on root tab screens)
    val showBottomBar = currentRoute == Screen.Home.route || currentRoute == Screen.MapPlaceholder.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                        label = { Text("Beranda") },
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            if (currentRoute != Screen.Home.route) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Map, contentDescription = "Peta") },
                        label = { Text("Peta") },
                        selected = currentRoute == Screen.MapPlaceholder.route,
                        onClick = {
                            if (currentRoute != Screen.MapPlaceholder.route) {
                                navController.navigate(Screen.MapPlaceholder.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // ── Tab 1: Home Screen ──────────────────────────────────────────
            composable(Screen.Home.route) {
                HomeScreen(
                    onPlaceClick = { placeId ->
                        navController.navigate(Screen.Detail.createRoute(placeId))
                    },
                    // If user clicks a category chip on Home, we can either filter in-place
                    // or navigate to PlaceListScreen. Let's make HomeScreen filter in-place,
                    // but we also support going to PlaceListScreen via another action if needed.
                    // For maximum completeness, let's allow HomeScreen to navigate to PlaceList
                    // if category is selected, or keep it inside.
                )
            }

            // ── Tab 2: Map Screen ───────────────────────────────────────────
            composable(Screen.MapPlaceholder.route) {
                MapScreen(
                    placeRepository = placeRepository,
                    onOpenRoute = onOpenRoute
                )
            }

            // ── Place List Screen ───────────────────────────────────────────
            composable(
                route = Screen.PlaceList.route,
                arguments = listOf(
                    navArgument("categorySlug") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("categoryName") {
                        type = NavType.StringType
                        defaultValue = "Daftar Tempat"
                    }
                )
            ) {
                PlaceListScreen(
                    onPlaceClick = { placeId ->
                        navController.navigate(Screen.Detail.createRoute(placeId))
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // ── Place Detail Screen ─────────────────────────────────────────
            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("placeId") {
                        type = NavType.IntType
                    }
                )
            ) {
                DetailScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onOpenRoute = onOpenRoute
                )
            }
        }
    }
}
