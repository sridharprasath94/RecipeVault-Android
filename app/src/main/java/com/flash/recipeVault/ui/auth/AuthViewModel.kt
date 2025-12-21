@file:Suppress("DEPRECATION")

package com.flash.recipeVault.ui.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.GoogleAuthProvider


sealed class AuthState {
    data object Loading : AuthState()
    data object LoggedOut : AuthState()
    data class LoggedIn(val uid: String, val email: String?) : AuthState()
    data class Error(val message: String) : AuthState()
}

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val message: String? = null, // validation/user-facing message
)

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state

    private val _form = MutableStateFlow(AuthFormState())
    val form: StateFlow<AuthFormState> = _form

    fun onEmailChange(v: String) {
        _form.value = _form.value.copy(email = v, message = null)
    }

    fun onPasswordChange(v: String) {
        _form.value = _form.value.copy(password = v, message = null)
    }

    fun clearMessage() {
        if (_form.value.message != null) _form.value = _form.value.copy(message = null)
    }

    private val authListener = FirebaseAuth.AuthStateListener { refresh() }

    init {
        refresh()
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)
        super.onCleared()
    }

    private fun refresh() {
        val user = auth.currentUser
        _state.value =
            if (user == null) AuthState.LoggedOut else AuthState.LoggedIn(user.uid, user.email)
    }

    fun signIn(email: String, password: String) = viewModelScope.launch {
        val cleanEmail = email.trim()
        val cleanPass = password.trim()

        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            _form.value = _form.value.copy(message = "Email and password are required")
            _state.value = AuthState.LoggedOut
            return@launch
        }

        try {
            _state.value = AuthState.Loading
            auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
            refresh()
        } catch (e: Exception) {
            _state.value = AuthState.Error(e.message ?: "Sign-in failed")
        }
    }

    fun signUp(email: String, password: String) = viewModelScope.launch {
        val cleanEmail = email.trim()
        val cleanPass = password.trim()

        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            _form.value = _form.value.copy(message = "Email and password are required")
            _state.value = AuthState.LoggedOut
            return@launch
        }

        try {
            _state.value = AuthState.Loading
            auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
            refresh()
        } catch (e: Exception) {
            _state.value = AuthState.Error(e.message ?: "Sign-up failed")
        }
    }

    fun submitSignIn() = signIn(form.value.email, form.value.password)

    fun submitSignUp() = signUp(form.value.email, form.value.password)

    fun setError(message: String) {
        _state.value = AuthState.Error(message)
    }

    fun clearError() {
        if (_state.value is AuthState.Error) refresh()
    }

    fun signInWithGoogleIdToken(idToken: String) = viewModelScope.launch {
        try {
            _state.value = AuthState.Loading
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            clearMessage()
            Log.d("AuthViewModel", "Google sign-in successful")
            refresh()
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Google sign-in failed", e)
            _state.value = AuthState.Error(e.message ?: "Google sign-in failed")
        }
    }


    fun onGoogleResult(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            setError(
                if (resultCode == Activity.RESULT_CANCELED) {
                    "Google sign-in was cancelled. Check Firebase Google provider + SHA-1 + google-services.json."
                } else {
                    "Google sign-in failed (resultCode=$resultCode)."
                }
            )
            return
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                setError("Google sign-in returned blank idToken. Check default_web_client_id + SHA-1.")
                return
            }
            signInWithGoogleIdToken(idToken)
        } catch (e: ApiException) {
            setError("Google sign-in failed: ${e.localizedMessage ?: "Unknown error"}")
        } catch (e: Exception) {
            setError("Google sign-in failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
