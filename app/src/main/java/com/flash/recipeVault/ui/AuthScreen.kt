@file:Suppress("DEPRECATION")

package com.flash.recipeVault.ui

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.R
import com.flash.recipeVault.ui.theme.RecipeSaverTheme
import com.flash.recipeVault.vm.AuthState
import com.flash.recipeVault.vm.AuthContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authVm: AuthContract,
    onLoggedIn: () -> Unit
) {
    val state by authVm.state.collectAsState()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

    // Requires a real google-services.json to generate R.string.default_web_client_id
    val webClientId = stringResource(R.string.default_web_client_id)

    val googleClient = remember(webClientId) {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .apply { if (webClientId.isNotBlank()) requestIdToken(webClientId) }
                .build()
        )
    }

    var googleConfigDialog by remember { mutableStateOf(false) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            Log.w("LoginScreen", "Google sign-in canceled. resultCode=${result.resultCode}")
            authVm.setError(
                if (result.resultCode == Activity.RESULT_CANCELED) {
                    "Google sign-in was cancelled. If you selected an account and it still cancels, check Firebase Google provider + SHA-1 + google-services.json."
                } else {
                    "Google sign-in failed (resultCode=${result.resultCode})."
                }
            )
            return@rememberLauncherForActivityResult
        }
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                Log.e(
                    "LoginScreen",
                    "Google sign-in returned null/blank idToken. Check default_web_client_id + SHA-1 in Firebase."
                )
                authVm.clearError()
                return@rememberLauncherForActivityResult
            }

            Log.d("LoginScreen", "Google sign-in success. email=${account.email}")
            authVm.signInWithGoogleIdToken(idToken)
        } catch (e: ApiException) {
            Log.e("LoginScreen", "Google sign-in failed. statusCode=${e.statusCode}", e)
            // Surface the error through the existing AuthState.Error UI by setting an error
            authVm.setError("Google sign-in failed: ${e.localizedMessage ?: "Unknown error"}")
        } catch (e: Exception) {
            Log.e("LoginScreen", "Google sign-in unexpected error", e)
            authVm.setError("Google sign-in failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    LaunchedEffect(state) {
        if (state is AuthState.LoggedIn) onLoggedIn()
    }

    if (googleConfigDialog) {
        AlertDialog(
            onDismissRequest = { googleConfigDialog = false },
            title = { Text("Google Sign-In setup required") },
            text = { Text("Replace app/google-services.json with your real Firebase config (and ensure default_web_client_id is generated).") },
            confirmButton = { TextButton(onClick = { googleConfigDialog = false }) { Text("OK") } }
        )
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            when (val s = state) {
                is AuthState.Error -> {
                    Text(s.message)
                    TextButton(onClick = { authVm.clearError() }) { Text("Dismiss") }
                }

                AuthState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                else -> {}
            }

            Spacer(Modifier.height(60.dp))

            Button(
                onClick = { authVm.signIn(email.trim(), password) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Sign in") }

            OutlinedButton(
                onClick = { authVm.signUp(email.trim(), password) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Create account") }

            OutlinedButton(
                onClick = {
                    if (webClientId.isBlank()) googleConfigDialog = true
                    else googleLauncher.launch(googleClient.signInIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Sign in with Google") }
        }
    }
}

@Preview
@Composable
fun AuthScreenPreview() {
    val fake = object : AuthContract {
        override val state = MutableStateFlow<AuthState>(AuthState.LoggedOut)

        override fun signIn(email: String, password: String): Job =
            Job().apply { complete() }

        override fun signUp(email: String, password: String): Job =
            Job().apply { complete() }

        override fun signInWithGoogleIdToken(idToken: String): Job =
            Job().apply { complete() }

        override fun clearError() {}
        override fun setError(message: String) {}
    }

    RecipeSaverTheme {
        AuthScreen(authVm = fake, onLoggedIn = {})
    }
}

