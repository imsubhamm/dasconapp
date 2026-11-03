package com.dascon.machinerylog.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dascon.machinerylog.data.repo.LogRepository
import com.dascon.machinerylog.ui.BrandLogo
import com.dascon.machinerylog.ui.theme.DscplBlue
import com.dascon.machinerylog.ui.theme.DscplInk
import com.dascon.machinerylog.ui.theme.DscplLine
import com.dascon.machinerylog.ui.theme.DscplMist
import com.dascon.machinerylog.ui.theme.DscplMuted
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repository: LogRepository,
    onLoggedIn: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = DscplInk,
        unfocusedTextColor = DscplInk,
        focusedBorderColor = DscplBlue,
        unfocusedBorderColor = DscplLine,
        focusedLabelColor = DscplBlue,
        unfocusedLabelColor = DscplMuted,
        cursorColor = DscplBlue,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DscplMist)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BrandLogo(logoSize = 120.dp)

        Spacer(Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, DscplLine, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(20.dp),
        ) {
            Text(
                "Sign in",
                color = DscplInk,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
            Text(
                "Use your Dascon ERP email & password",
                color = DscplMuted,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = fieldColors,
                textStyle = androidx.compose.ui.text.TextStyle(color = DscplInk, fontSize = 17.sp),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = fieldColors,
                textStyle = androidx.compose.ui.text.TextStyle(color = DscplInk, fontSize = 17.sp),
            )

            if (error != null) {
                Text(
                    error!!,
                    color = Color(0xFFB42318),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        error = "Enter email and password"
                        return@Button
                    }
                    loading = true
                    error = null
                    scope.launch {
                        val result = repository.login(email, password)
                        loading = false
                        result.onSuccess { onLoggedIn() }
                            .onFailure { error = it.message ?: "Login failed" }
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DscplBlue,
                    contentColor = Color.White,
                ),
            ) {
                if (loading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.height(22.dp))
                } else {
                    Text("Sign in", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
