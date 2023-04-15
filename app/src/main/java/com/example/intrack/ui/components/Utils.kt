package com.example.intrack.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(
    password: String,
    onTextChanged: (String) -> Unit,
    isPasswordVisible: Boolean,
    onPasswordVisibilityChanged: () -> Unit,
    isPasswordValid: Boolean,
) {
    OutlinedTextField(
        value = password,
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
        },
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailTextField(email: String, onTextChanged: (String) -> Unit) {
    OutlinedTextField(
        value = email,
        onValueChange = onTextChanged,
        label = { Text(text = "Email") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputBox(value: String, onTextChanged: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onTextChanged,
        label = { Text(text = label) },
        modifier = Modifier.fillMaxWidth()
    )
}