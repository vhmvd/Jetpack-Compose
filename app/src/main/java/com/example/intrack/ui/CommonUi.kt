package com.example.intrack.ui

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class InTrackScreen(val title: String) {
    Home(title = "Home"),
    Requests(title = "Requests")
}

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InTrackAppBar(
    currentScreen: InTrackScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    logout: () -> Unit
) {
    TopAppBar(title = { Text(currentScreen.title) }, modifier = modifier, navigationIcon = {
        if (canNavigateBack) {
            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, contentDescription = "Back button"
                )
            }
        }
    }, actions = {
        Icon(imageVector = Icons.Filled.Logout,
            contentDescription = "Logout",
            modifier = Modifier.clickable {
                logout()
            })
    })
}
