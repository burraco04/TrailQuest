package com.example.trailquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.trailquest.data.auth.AuthRepository
import com.example.trailquest.data.db.AppDatabase
import com.example.trailquest.data.pref.SettingsRepository
import com.example.trailquest.ui.*
import com.example.trailquest.ui.theme.TrailQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "trailquest-db"
        ).build()
        
        val authRepository = AuthRepository()
        val settingsRepository = SettingsRepository(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(authRepository, db.trailDao(), settingsRepository)
            )
            
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentUser by viewModel.currentUser.collectAsState()

            TrailQuestTheme(darkTheme = isDarkMode) {
                if (currentUser == null) {
                    LoginScreen(
                        onLogin = { email, password, onResult ->
                            viewModel.login(
                                email = email,
                                password = password,
                                onResult = onResult
                            )
                        },
                        onRegister = { name, email, password, onResult ->
                            viewModel.register(
                                name = name,
                                email = email,
                                password = password,
                                onResult = onResult
                            )
                        }
                    )
                } else {
                    TrailQuestApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun TrailQuestApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val trails by viewModel.trails.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val totalPoints by viewModel.totalPoints.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var activeHikeTrail by remember { mutableStateOf<com.example.trailquest.data.model.Trail?>(null) }

    if (activeHikeTrail != null) {
        HikeScreen(
            trail = activeHikeTrail!!, 
            onEndHike = { distance -> 
                viewModel.completeHike(activeHikeTrail!!, distance)
                activeHikeTrail = null 
            }
        )
    } else {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
                        icon = { Icon(it.icon, contentDescription = it.label) },
                        label = { Text(it.label) },
                        selected = currentRoute == it.route,
                        onClick = {
                            navController.navigate(it.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) {
            Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = AppDestinations.HOME.route,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(AppDestinations.HOME.route) {
                        TrailListScreen(
                            trails = trails,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                            onTrailClick = { trail ->
                                navController.navigate("detail/${trail.id}")
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                    }
                    composable("detail/{trailId}") { backStackEntry ->
                        val trailId = backStackEntry.arguments?.getString("trailId")
                        val trail = trails.find { it.id == trailId }
                        TrailDetailScreen(trail = trail,
                            onStartHike = {
                                activeHikeTrail = trail
                            },
                            onToggleFavorite = {
                                if (trail != null) {
                                    viewModel.toggleFavorite(trail)
                                }
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(AppDestinations.PROFILE.route) {
                        ProfileScreen(
                            username = currentUser?.displayName ?: "User",
                            email = currentUser?.email ?: "",
                            points = totalPoints ?: 0,
                            onLogout = { viewModel.logout() }
                        )
                    }
                    composable(AppDestinations.SETTINGS.route) {
                        SettingsScreen(
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { viewModel.toggleDarkMode(it) }
                        )
                    }
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    HOME("Home", Icons.Default.Home, "home"),
    PROFILE("Profile", Icons.Default.AccountBox, "profile"),
    SETTINGS("Settings", Icons.Default.Settings, "settings"),
}

class MainViewModelFactory(
    private val authRepository: AuthRepository,
    private val trailDao: com.example.trailquest.data.db.TrailDao,
    private val settingsRepository: SettingsRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(authRepository, trailDao, settingsRepository) as T
    }
}
