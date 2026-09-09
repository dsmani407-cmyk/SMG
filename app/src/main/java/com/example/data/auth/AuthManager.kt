package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

data class AuthUserResult(
    val uid: String,
    val email: String?,
    val displayName: String?
)

object AuthManager {
    private const val TAG = "AuthManager"

    val isFirebaseInitialized: Boolean
        get() {
            return try {
                FirebaseApp.getApps(FirebaseAuth.getInstance().app.applicationContext).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }

    val firebaseAuth: FirebaseAuth?
        get() {
            return try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseAuth not initialized: ${e.message}")
                null
            }
        }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth?.currentUser

    /**
     * Signs in using Firebase Auth with individual email and password.
     * Supports either full email (user@example.com) or username mapped to domain.
     */
    suspend fun signInWithEmailPassword(
        identifier: String,
        password: String
    ): Result<AuthUserResult> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase Auth not available"))
        val email = formatToEmail(identifier)

        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                Result.success(
                    AuthUserResult(
                        uid = user.uid,
                        email = user.email,
                        displayName = user.displayName
                    )
                )
            } else {
                Result.failure(Exception("Authentication succeeded but user was null"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase signInWithEmailPassword failed", e)
            Result.failure(e)
        }
    }

    /**
     * Creates a new Firebase Auth account for a newly registered distributor.
     */
    suspend fun createUserWithEmailPassword(
        identifier: String,
        password: String,
        displayName: String? = null
    ): Result<AuthUserResult> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase Auth not available"))
        val email = formatToEmail(identifier)

        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                Result.success(
                    AuthUserResult(
                        uid = user.uid,
                        email = user.email,
                        displayName = displayName ?: user.displayName
                    )
                )
            } else {
                Result.failure(Exception("Creation succeeded but user was null"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase createUserWithEmailPassword failed", e)
            Result.failure(e)
        }
    }

    /**
     * Sends password reset email via Firebase Auth's sendPasswordResetEmail.
     */
    suspend fun sendPasswordReset(identifier: String): Result<String> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase Auth service is not initialized"))
        val email = formatToEmail(identifier)
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(email)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase sendPasswordReset failed for $email", e)
            val message = when {
                e.message?.contains("no user record", ignoreCase = true) == true ||
                e.message?.contains("user-not-found", ignoreCase = true) == true ->
                    "No user found with $email in Firebase Auth."
                e.message?.contains("badly formatted", ignoreCase = true) == true ||
                e.message?.contains("invalid-email", ignoreCase = true) == true ->
                    "Invalid email address format."
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Network connection issue. Please check your connection and try again."
                else -> e.localizedMessage ?: "Failed to send password reset email."
            }
            Result.failure(Exception(message, e))
        }
    }

    /**
     * Google Sign-In using Credential Manager and Firebase Auth.
     */
    suspend fun signInWithGoogleCredentialManager(
        context: Context,
        serverClientId: String? = null
    ): Result<AuthUserResult> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase Auth not available"))
        val credentialManager = CredentialManager.create(context)

        return try {
            val clientId = serverClientId ?: "smartgroup-auth.apps.googleusercontent.com"
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    Result.success(
                        AuthUserResult(
                            uid = user.uid,
                            email = user.email,
                            displayName = user.displayName
                        )
                    )
                } else {
                    Result.failure(Exception("Google sign in completed but user was null"))
                }
            } else {
                Result.failure(Exception("Unsupported credential type"))
            }
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Credential Manager flow canceled or failed: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Error signing out from Firebase: ${e.message}")
        }
    }

    fun formatToEmail(identifier: String): String {
        return if (identifier.contains("@")) {
            identifier.trim().lowercase()
        } else {
            "${identifier.trim().lowercase()}@smartgroup.app"
        }
    }
}
