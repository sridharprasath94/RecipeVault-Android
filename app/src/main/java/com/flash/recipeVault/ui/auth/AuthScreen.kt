@file:Suppress("DEPRECATION")

package com.flash.recipeVault.ui.auth

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.R
import com.flash.recipeVault.ui.components.StandardTextField
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authVm: AuthViewModel,
    onLoggedIn: () -> Unit
) {
    val state by authVm.state.collectAsState()
    val form by authVm.form.collectAsState()
    val context = LocalContext.current
    val uid = (state as? AuthState.LoggedIn)?.uid

    // Requires a real google-services.json to generate R.string.default_web_client_id
    val webClientId = stringResource(R.string.default_web_client_id)
    val googleClient = remember(context, webClientId) { googleSignInClient(webClientId, context) }

    var googleConfigDialog by remember { mutableStateOf(false) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        authVm.onGoogleResult(result.resultCode, result.data)
    }

    LaunchedEffect(uid) {
        if (uid != null) onLoggedIn()
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
        AuthFormContent(
            padding = padding,
            email = form.email,
            onEmailChange = authVm::onEmailChange,
            password = form.password,
            onPasswordChange = authVm::onPasswordChange,
            state = state,
            onDismissError = { authVm.clearError() },
            onSignIn = authVm::submitSignIn,
            onSignUp = authVm::submitSignUp,
            onGoogleSignIn = {
                if (webClientId.isBlank()) googleConfigDialog = true
                else googleLauncher.launch(googleClient.signInIntent)
            }
        )
    }
}


fun googleSignInClient(id: String, context: Context): GoogleSignInClient = GoogleSignIn.getClient(
    context,
    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .apply { if (id.isNotBlank()) requestIdToken(id) }
        .build()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthFormContent(
    padding: PaddingValues,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    state: AuthState,
    onDismissError: () -> Unit,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    onGoogleSignIn: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(20.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(
            space = 12.dp,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StandardTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardType = KeyboardType.Email,
        )

        StandardTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        when (state) {
            is AuthState.Error -> {
                Text(state.message)
                TextButton(onClick = onDismissError) { Text("Dismiss") }
            }

            is AuthState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            is AuthState.LoggedIn -> Unit
            is AuthState.LoggedOut -> Unit
        }

        Spacer(Modifier.height(60.dp))

        Button(
            onClick = onSignIn,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Sign in") }

        OutlinedButton(
            onClick = onSignUp,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Create account") }

        OutlinedButton(
            onClick = onGoogleSignIn,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Sign in with Google") }
    }
}



