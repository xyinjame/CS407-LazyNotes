package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cs407.lazynotes.R
import com.cs407.lazynotes.data.NoteDatabase
import com.cs407.lazynotes.data.User
import com.cs407.lazynotes.data.UserState
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ErrorText(error: String?, modifier: Modifier = Modifier) {
    if (error != null)
        Text(text = error, color = Color.Red, textAlign = TextAlign.Center)
}

@Composable
fun userEmail(modifier: Modifier = Modifier): String {
    var email by remember { mutableStateOf("") }
    TextField(
        value = email,
        onValueChange = { email = it },
        label = { Text(stringResource(R.string.email_hint)) })
    return email
}

@Composable
fun userPassword(modifier: Modifier = Modifier): String {
    var passwd by remember { mutableStateOf("") }
    TextField(
        value = passwd,
        onValueChange = { passwd = it },
        label = { Text(stringResource(R.string.password_hint)) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
    return passwd
}

fun createAccount(
    email: String,
    password: String,
    auth: FirebaseAuth,
    onComplete: (Boolean, Exception?, FirebaseUser?) -> Unit,
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            onComplete(task.isSuccessful, task.exception, auth.currentUser)
        }
}

fun signIn(
    email: String,
    password: String,
    auth: FirebaseAuth,
    onComplete: (Boolean, Exception?, FirebaseUser?) -> Unit,
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful)
                onComplete(task.isSuccessful, task.exception, auth.currentUser)
            else
                createAccount(email, password, auth, onComplete)
        }
}

fun signOutCustomAuth(auth: FirebaseAuth) {
    auth.signOut()
}

fun updateName(
    name: String,
    auth: FirebaseAuth,
    onComplete: (Boolean, Exception?, FirebaseUser?) -> Unit
) {
    val user = auth.currentUser
    val profileUpdates = userProfileChangeRequest {
        displayName = name
    }
    user?.updateProfile(profileUpdates)
        ?.addOnCompleteListener { task ->
            onComplete(task.isSuccessful, task.exception, user)
        }
}

@Composable
fun getYourFirebaseAuth(): FirebaseAuth {
    val context = LocalContext.current
    return remember(key1 = "custom-firebase-auth") {
        try {
            val yourOptions = FirebaseOptions.Builder()
                .setApiKey("AIzaSyADfafGhxgAVMd6SnrlPay_iGBRuO6Awe4")
                .setApplicationId("1:1097491779682:android:cb89f65fe146552ff3813b")
                .setProjectId("lazynotes-83b8d")
                .build()

            val customFirebaseAppName = "login-firebase-instance"
            if (FirebaseApp.getApps(context).none { it.name == customFirebaseAppName }) {
                FirebaseApp.initializeApp(context, yourOptions, customFirebaseAppName)
            }
            FirebaseAuth.getInstance(FirebaseApp.getInstance(customFirebaseAppName))
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseAuth.getInstance()
        }
    }
}

@Composable
fun AskNamePage(
    auth: FirebaseAuth,
    onComplete: (Boolean, Exception?, FirebaseUser?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    TextField(
        value = name,
        onValueChange = { name = it },
        label = { Text(stringResource(R.string.name_hint)) })
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = {
        updateName(name, auth, onComplete)
    }) {
        Text(stringResource(R.string.confirm_button))
    }
}

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    loginButtonClick: (UserState) -> Unit
) {
    var email: String
    var password: String
    var error: String? by remember { mutableStateOf(null) }
    var name: Boolean by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val noteDB = NoteDatabase.getDatabase(context)
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val yourAuth = getYourFirebaseAuth()
    val currentUser by remember(yourAuth) { mutableStateOf(yourAuth.currentUser) }

    suspend fun ensureLocalUser(uid: String): User {
        val existing = noteDB.userDao().getByUID(uid)
        if (existing != null) return existing
        noteDB.userDao().insert(User(userUID = uid))
        return noteDB.userDao().getByUID(uid)
            ?: throw IllegalStateException("Failed to create local user record")
    }

    val onComplete: (Boolean, Exception?, FirebaseUser?) -> Unit =
        { isSuccess, taskException, signedUser ->
            if (isSuccess && signedUser != null) {
                if (!signedUser.displayName.isNullOrEmpty()) {
                    scope.launch {
                        try {
                            val localUser = ensureLocalUser(signedUser.uid)
                            loginButtonClick(
                                UserState(
                                    id = localUser.userId,
                                    name = signedUser.displayName ?: "",
                                    uid = signedUser.uid
                                )
                            )
                        } catch (e: Exception) {
                            error = e.message
                        }
                    }
                } else {
                    name = true
                }
            } else {
                error = taskException?.message
            }
        }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onComplete(true, null, currentUser)
        }
    }

    Scaffold(modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!name) {
                ErrorText(error)
                email = userEmail()
                password = userPassword()
                Spacer(modifier = Modifier.height(16.dp))
                LogInSignUpButton(email, password, yourAuth, onComplete)
            } else {
                AskNamePage(auth = yourAuth, onComplete = onComplete)
            }
        }
    }
}

@Composable
fun LogInSignUpButton(
    email: String,
    password: String,
    auth: FirebaseAuth,
    onComplete: (Boolean, Exception?, FirebaseUser?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Button(onClick = {
        var errorString: String? = null

        val emailResult = checkEmail(email)
        if (emailResult == EmailResult.Empty) {
            errorString = context.getString(R.string.empty_email)
        } else if (emailResult == EmailResult.Invalid) {
            errorString = context.getString(R.string.invalid_email)
        }

        val passwordResult = checkPassword(password)
        if (errorString == null) {
            errorString = when (passwordResult) {
                PasswordResult.Empty -> context.getString(R.string.empty_password)
                PasswordResult.Short -> context.getString(R.string.short_password)
                PasswordResult.Invalid -> context.getString(R.string.invalid_password)
                PasswordResult.Valid -> null
            }
        }

        if (errorString != null)
            onComplete(false, Exception(errorString), null)
        else
            signIn(email, password, auth, onComplete)
    }) {
        Text(stringResource(R.string.login_button))
    }
}

enum class EmailResult {
    Valid, Empty, Invalid
}

fun checkEmail(email: String): EmailResult {
    if (email.isEmpty()) return EmailResult.Empty
    val pattern = Regex("^[\\w.]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$")
    return if (pattern.matches(email)) EmailResult.Valid else EmailResult.Invalid
}

enum class PasswordResult {
    Valid, Empty, Short, Invalid
}

fun checkPassword(password: String): PasswordResult {
    if (password.isEmpty()) return PasswordResult.Empty
    if (password.length < 5) return PasswordResult.Short
    val hasDigit = Regex("\\d+").containsMatchIn(password)
    val hasLowercase = Regex("[a-z]+").containsMatchIn(password)
    val hasUppercase = Regex("[A-Z]+").containsMatchIn(password)
    return if (hasDigit && hasLowercase && hasUppercase) PasswordResult.Valid else PasswordResult.Invalid
}