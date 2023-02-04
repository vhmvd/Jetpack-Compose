package com.example.intrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.*
import com.example.intrack.ui.AppViewModel
import com.example.intrack.ui.InTrackAppBar
import com.example.intrack.ui.InTrackScreen
import com.example.intrack.ui.theme.InTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
//    private lateinit var viewModel: AppViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val firebase = FirebaseAuth.getInstance()
            val currentUser = firebase.currentUser
            InTrackTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    if (currentUser == null) {
                        LogInOrSignUpScreen()
                    } else {
                        val snackbarHostState = remember { SnackbarHostState() }
                        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
                            InTrackAppBar(currentScreen = InTrackScreen.Home,
                                canNavigateBack = false,
                                navigateUp = { })
                        }, bottomBar = {
                            var selectedItem by remember { mutableStateOf(0) }
                            val items = listOf("Home", "Notifications")

                            NavigationBar {
                                items.forEachIndexed { index, item ->
                                    NavigationBarItem(icon = {
                                        Icon(
                                            if (index == 0) {
                                                Icons.Filled.Home
                                            } else {
                                                Icons.Filled.Notifications
                                            }, contentDescription = item
                                        )
                                    },
                                        label = { Text(item) },
                                        selected = selectedItem == index,
                                        onClick = { selectedItem = index })
                                }
                            }
                        }, content = { paddingValues ->
                            paddingValues
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun NormalFlow() {
    InTrackTheme {
        val navController = rememberNavController()
        NavGraph(navController)
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable(route = "login") {
            //call LoginScreen composable function here
        }

        composable(route = "home") {
            //call HomeScreen composable function here
        }
    }
}

@Composable
fun LogInOrSignUpScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordValid by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }

    // Lottie
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.truck))
    val progress by animateLottieCompositionAsState(composition = composition, iterations = 5)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    SnackbarHost(hostState = snackbarHostState, modifier = Modifier)

    Box(contentAlignment = Alignment.Center) {
        when (loading) {
            true -> CircularProgressIndicator(
                Modifier
                    .zIndex(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = Color.White)
                    .border(
                        width = 0.dp,
                        shape = RoundedCornerShape(8.dp),
                        brush = SolidColor(value = Color.LightGray)
                    )
            )
            false -> Unit
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .padding(16.dp)
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                alignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            )
            EmailTextField(email = email, onTextChanged = { email = it })
            PasswordTextField(
                password = password,
                onTextChanged = {
                    password = it
                    isPasswordValid = password.length >= 6
                },
                isPasswordVisible = isPasswordVisible,
                onPasswordVisibilityChanged = { isPasswordVisible = isPasswordVisible.not() },
                isPasswordValid = isPasswordValid,
            )

            Button(
                onClick = {
                    loading = true
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener {
                            loading = false
                            if (it.isSuccessful) {
                                // Login successful, navigate to main activity
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = (it.exception as FirebaseAuthException).localizedMessage
                                            ?: "Try again later"
                                    )
                                }
                            }
                        }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp)
            ) {
                Text(text = "Login")
            }
            Divider(thickness = 1.dp, modifier = Modifier.padding(8.dp))
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp)
            ) {
                Text(text = "Create Account")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(
    password: String,
    onTextChanged: (String) -> Unit,
    isPasswordVisible: Boolean,
    onPasswordVisibilityChanged: () -> Unit,
    isPasswordValid: Boolean,
) {
    OutlinedTextField(value = password,
        onValueChange = onTextChanged,
        label = { Text(text = "Password") },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (isPasswordVisible) Icons.Filled.Visibility
            else Icons.Filled.VisibilityOff
            val description = if (isPasswordVisible) "Hide password" else "Show password"
            IconButton(onClick = onPasswordVisibilityChanged) {
                Icon(imageVector = image, description)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        isError = isPasswordValid.not(),
        supportingText = {
            if (isPasswordValid.not()) {
                Text(text = "Password length should be at least 6 characters.")
            }
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailTextField(email: String, onTextChanged: (String) -> Unit) {
    OutlinedTextField(
        value = email,
        onValueChange = onTextChanged,
        label = { Text(text = "Email") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun DefaultPreview() {
    LogInOrSignUpScreen()
}