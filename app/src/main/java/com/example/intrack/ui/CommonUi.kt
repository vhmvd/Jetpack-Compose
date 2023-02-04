package com.example.intrack.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class InTrackScreen(val title: String) {
    Home(title = "Home"), Add(title = "Add Assets"), Location(title = "Location"), Settings(title = "Settings"), QR(
        title = "QR"
    ),
    Notifications(title = "Notifications")
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
    modifier: Modifier = Modifier
) {
    TopAppBar(title = { Text(currentScreen.title) }, modifier = modifier, navigationIcon = {
        if (canNavigateBack) {
            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, contentDescription = "Back button"
                )
            }
        }
    })
}
