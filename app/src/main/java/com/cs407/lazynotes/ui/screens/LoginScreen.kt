package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cs407.lazynotes.R
import com.cs407.lazynotes.data.FolderRepository
import com.cs407.lazynotes.data.NoteDatabase
import com.cs407.lazynotes.data.NoteRepository
import com.cs407.lazynotes.data.User
import com.cs407.lazynotes.data.UserState
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import androidx.compose.runtime.rememberCoroutineScope

//Test Comment
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

fun deleteUserAccount(
    auth: FirebaseAuth,
    onComplete: (Boolean, Exception?) -> Unit
) {
    val user = auth.currentUser
    user?.delete()
        ?.addOnCompleteListener { task->
            if (task.isSuccessful) {
                println("User account deleted")
                onComplete(true, null)
            } else {
                println("Failed to delete account: ${task.exception?.message}")
                onComplete(false, task.exception)
            }
        }
}

suspend fun deleteLocalUserData(context: android.content.Context, userUID: String) {
    try {
        val noteDB = NoteDatabase.getDatabase(context)
        val user = noteDB.userDao().getByUID(userUID)
        if (user != null) {
            noteDB.deleteDao().delete(user.userId)
            println("✅ Local user data deleted")
        }
    } catch (e: Exception) {
        println("Failed to delete local user data: ${e.message}")
    }
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

    val primary = colorResource(id = R.color.primary_blue)
    val accent = colorResource(id = R.color.accent_coral)
    val background = colorResource(id = R.color.background_light)
    val surface = colorResource(id = R.color.surface_white)
    val textPrimary = colorResource(id = R.color.text_primary)
    val textSecondary = colorResource(id = R.color.text_secondary)

    var name by remember { mutableStateOf("") }

    Scaffold(
        containerColor = background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Welcome!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = primary
                    )

                    Text(
                        text = "Please enter your name to continue",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.name_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primary,
                            focusedLabelColor = primary,
                            cursorColor = primary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            updateName(name, auth, onComplete)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = name.isNotBlank()
                    ) {
                        Text(
                            stringResource(R.string.confirm_button),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    loginButtonClick: (UserState) -> Unit
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error: String? by remember { mutableStateOf(null) }
    var name: Boolean by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val noteDB = NoteDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val primary = colorResource(id = R.color.primary_blue)
    val accent = colorResource(id = R.color.accent_coral)
    val background = colorResource(id = R.color.background_light)
    val surface = colorResource(id = R.color.surface_white)
    val textPrimary = colorResource(id = R.color.text_primary)
    val textSecondary = colorResource(id = R.color.text_secondary)

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

                            FolderRepository.initialize(context,localUser.userId)
                            NoteRepository.initialize(context, localUser.userId)

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

    Scaffold(
        modifier = modifier,
        containerColor = background
    ) { innerPadding ->
        if (!name) {
            Box (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(background),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = surface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LazyNotes",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )

                        Text(
                            text = "Sign up to continue",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (error != null) {
                            Text(
                                text = error!!,
                                color = accent,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                error = null
                            },
                            label = { Text(stringResource(R.string.email_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primary,
                                focusedLabelColor = primary,
                                cursorColor = primary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                error = null
                            },
                            label = { Text(stringResource(R.string.password_hint)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primary,
                                focusedLabelColor = primary,
                                cursorColor = primary
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
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

                                if (errorString != null) {
                                    error = errorString
                                } else {
                                    signIn(email, password, yourAuth, onComplete)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                stringResource(R.string.login_button),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            AskNamePage(auth = yourAuth, onComplete = onComplete)
        }
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