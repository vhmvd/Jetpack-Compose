package com.example.intrack.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.example.intrack.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun LogInOrSignUpScreen(navController: NavHostController, onInitViewModel: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordValid by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    SnackbarHost(hostState = snackbarHostState, modifier = Modifier)

    Box(
        contentAlignment = Alignment.Center, modifier = Modifier.background(color = Color.White)
    ) {
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
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
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
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener {
                            loading = false
                            if (it.isSuccessful) {
                                onInitViewModel()
                                navController.navigate("home") {
                                    popUpTo("login") {
                                        inclusive = true
                                    }
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = it.exception?.message ?: "Try again later"
                                    )
                                }
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp)
            ) {
                Text(text = "Login")
            }
            Divider(thickness = 1.dp, modifier = Modifier.padding(8.dp))
            Button(
                onClick = {
                    navController.navigate("register")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp)
            ) {
                Text(text = "Create Account")
            }
        }
    }
}