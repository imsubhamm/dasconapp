package com.dascon.machinerylog.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dascon.machinerylog.data.repo.LogRepository
import com.dascon.machinerylog.ui.BrandLogo
import com.dascon.machinerylog.ui.theme.DscplBlue
import com.dascon.machinerylog.ui.theme.DscplBlueDark
import com.dascon.machinerylog.ui.theme.DscplBlueMid
import com.dascon.machinerylog.ui.theme.DscplRed
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
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        DscplBlueDark,
                        DscplBlue,
                        DscplBlueMid.copy(alpha = 0.95f),
                    ),
                ),
            ),
    ) {
        // Soft radial accents
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = (-40).dp)
                .width(220.dp)
                .height(220.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                    ),
                    shape = RoundedCornerShape(110.dp),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 80.dp, start = (-60).dp)
                .width(260.dp)
                .height(260.dp)
                .background(
                    Brush.radialGradient(
                        listOf(DscplRed.copy(alpha = 0.18f), Color.Transparent),
                    ),
                    shape = RoundedCornerShape(130.dp),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "OFFICIAL SITE APP",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 18.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(24.dp, shape, spotColor = Color.Black.copy(alpha = 0.35f))
                    .clip(shape)
                    .background(Color.White)
                    .border(1.dp, Color.White.copy(alpha = 0.6f), shape)
                    .padding(horizontal = 24.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BrandLogo(logoSize = 104.dp)

                Spacer(Modifier.height(26.dp))

                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DscplBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = DscplBlue,
                    cursorColor = DscplBlue,
                    focusedContainerColor = Color(0xFFF8FAFF),
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("ERP email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = fieldColors,
                    textStyle = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = fieldColors,
                    textStyle = MaterialTheme.typography.bodyLarge,
                )

                if (error != null) {
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    )
                }

                Spacer(Modifier.height(22.dp))
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
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DscplBlueDark,
                        contentColor = Color.White,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.height(22.dp),
                        )
                    } else {
                        Text("Continue", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Text(
                    text = "Signed with your Dascon ERP credentials",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            Spacer(Modifier.height(22.dp))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(3.dp)
                    .background(DscplRed, RoundedCornerShape(2.dp)),
            )
            Text(
                text = "Dascon Sourav Commercial Pvt. Ltd.",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.72f),
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
