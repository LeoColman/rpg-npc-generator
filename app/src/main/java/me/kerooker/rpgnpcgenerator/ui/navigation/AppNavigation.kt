package me.kerooker.rpgnpcgenerator.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.ads.AdBanner
import me.kerooker.rpgnpcgenerator.ui.mynpcs.MyNpcsScreen
import me.kerooker.rpgnpcgenerator.ui.mynpcs.individual.NpcDetailScreen
import me.kerooker.rpgnpcgenerator.ui.random.RandomNpcScreen
import me.kerooker.rpgnpcgenerator.ui.settings.SettingsScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.reflect.KClass

@Serializable
object RandomNpcRoute

@Serializable
object MyNpcsRoute

@Serializable
object SettingsRoute

@Serializable
data class NpcDetailRoute(val id: Long)

private data class TopLevelTab(
    val route: Any,
    val routeClass: KClass<*>,
    @StringRes val labelRes: Int,
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null
)

private val topLevelTabs = listOf(
    TopLevelTab(
        RandomNpcRoute, RandomNpcRoute::class, R.string.nav_bar_random_npc,
        iconRes = R.drawable.ic_twenty_sided_dice
    ),
    TopLevelTab(MyNpcsRoute, MyNpcsRoute::class, R.string.nav_bar_my_npcs, icon = Icons.Filled.Groups),
    TopLevelTab(SettingsRoute, SettingsRoute::class, R.string.nav_bar_settings, icon = Icons.Filled.Settings)
)

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = topLevelTabs.any { tab ->
        currentDestination?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelTabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (tab.iconRes != null) {
                                    Icon(
                                        painter = painterResource(tab.iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else if (tab.icon != null) {
                                    Icon(tab.icon, contentDescription = null)
                                }
                            },
                            label = { Text(stringResource(tab.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = RandomNpcRoute,
                modifier = Modifier.weight(1f)
            ) {
                composable<RandomNpcRoute> {
                    RandomNpcScreen(viewModel = koinViewModel())
                }
                composable<MyNpcsRoute> {
                    MyNpcsScreen(
                        viewModel = koinViewModel(),
                        onNpcClick = { id -> navController.navigate(NpcDetailRoute(id)) }
                    )
                }
                composable<SettingsRoute> {
                    SettingsScreen(viewModel = koinViewModel())
                }
                composable<NpcDetailRoute> { entry ->
                    val route = entry.toRoute<NpcDetailRoute>()
                    NpcDetailScreen(
                        viewModel = koinViewModel { parametersOf(route.id) },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            // Global bottom banner (self-hides while the user's ad-free week is active).
            AdBanner()
        }
    }
}
