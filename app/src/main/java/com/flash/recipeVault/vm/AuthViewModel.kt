package com.flash.recipeVault.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Job

interface AuthContract {
    val state: StateFlow<AuthState>
    fun signIn(email: String, password: String): Job
    fun signUp(email: String, password: String): Job
    fun signInWithGoogleIdToken(idToken: String): Job
    fun clearError()
    fun setError(message: String)
}

sealed class AuthState {
    data object Loading : AuthState()
    data object LoggedOut : AuthState()
    data class LoggedIn(val uid: String, val email: String?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel(), AuthContract {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    override val state: StateFlow<AuthState> = _state

    init {
        refresh()
        auth.addAuthStateListener { refresh() }
    }

    private fun refresh() {
        val user = auth.currentUser
        _state.value = if (user == null) AuthState.LoggedOut else AuthState.LoggedIn(user.uid, user.email)
    }

    override fun signIn(email: String, password: String) = viewModelScope.launch {
        try {
            _state.value = AuthState.Loading
            auth.signInWithEmailAndPassword(email, password).await()
            refresh()
        } catch (e: Exception) {
            _state.value = AuthState.Error(e.message ?: "Sign-in failed")
        }
    }

    override fun signUp(email: String, password: String) = viewModelScope.launch {
        try {
            _state.value = AuthState.Loading
            auth.createUserWithEmailAndPassword(email, password).await()
            refresh()
        } catch (e: Exception) {
            _state.value = AuthState.Error(e.message ?: "Sign-up failed")
        }
    }

    override fun setError(message: String) {
        _state.value = AuthState.Error(message)
    }

    override fun clearError() {
        if (_state.value is AuthState.Error) _state.value = AuthState.LoggedOut
    }

    override fun signInWithGoogleIdToken(idToken: String) = viewModelScope.launch {
        try {
            _state.value = AuthState.Loading
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            Log.d("AuthViewModel", "Google sign-in successful")
            refresh()
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Google sign-in failed", e)
            _state.value = AuthState.Error(e.message ?: "Google sign-in failed")
        }
    }
}
