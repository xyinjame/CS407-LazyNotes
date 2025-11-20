package com.cs407.lazynotes.ui.screens

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import com.cs407.lazynotes.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UserState(
    val id: Int = 0,
    val name: String = "",
    val uid: String = ""
)

@Entity(
    indices = [Index(
        value = ["userUID"],
        unique = true
    )]
)
data class RoomUser(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val userUID: String = ""
)

@Dao
interface UserDao {
    @Query("SELECT * FROM RoomUser WHERE userUID = :uid")
    suspend fun getByUID(uid: String): RoomUser?

    @Insert
    suspend fun insert(user: RoomUser)

    @Query("DELETE FROM RoomUser WHERE userUID = :uid")
    suspend fun deleteByUID(uid: String)
}

@Dao
interface DeleteDao {
    @Transaction
    suspend fun deleteUser(uid: String) {
        deleteByUID(uid)
    }

    @Query("DELETE FROM RoomUser WHERE userUID = :uid")
    suspend fun deleteByUID(uid: String)
}

@Database(entities = [RoomUser::class], version = 1)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun deleteDao(): DeleteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "lazy_notes_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class UserViewModel(application: android.app.Application) : ViewModel() {
    private val appContext = application.applicationContext
    private val auth: FirebaseAuth = Firebase.auth
    private val _userState = MutableStateFlow(UserState())
    val userState = _userState.asStateFlow()
    val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        auth.addAuthStateListener { authResult ->
            val user = authResult.currentUser
            if (user != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    syncUser(user)
                }
            } else {
                resetState()
            }
        }
    }

    fun signInOrCreate(email: String, password: String) {
        viewModelScope.launch(Dispatchers.Main) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { signInTask ->
                    if (signInTask.isSuccessful) {
                        signInTask.result.user?.let { user ->
                            viewModelScope.launch(Dispatchers.IO) {
                                syncUser(user)
                            }
                        }
                    } else {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { createTask ->
                                if (createTask.isSuccessful) {
                                    createTask.result.user?.let { user ->
                                        viewModelScope.launch(Dispatchers.IO) {
                                            syncUser(user)
                                        }
                                    }
                                } else {
                                    _error.update { createTask.exception?.message ?: "Authentication failed" }
                                }
                            }
                    }
                }
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = auth.currentUser ?: throw Exception("No logged-in user")
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).result
                syncUser(user)
            } catch (e: Exception) {
                _error.update { e.message }
            }
        }
    }

    fun signOut() {
        auth.signOut()
        resetState()
    }

    fun deleteAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = auth.currentUser ?: throw Exception("No logged-in user")
                val uid = user.uid
                NoteDatabase.getInstance(appContext).deleteDao().deleteUser(uid)
                user.delete().result
                resetState()
            } catch (e: Exception) {
                _error.update { e.message }
            }
        }
    }

    private suspend fun syncUser(user: FirebaseUser) {
        withContext(Dispatchers.IO) {
            val uid = user.uid
            val name = user.displayName ?: ""
            val db = NoteDatabase.getInstance(appContext)
            val localUser = db.userDao().getByUID(uid) ?: run {
                val newUser = RoomUser(userUID = uid)
                db.userDao().insert(newUser)
                newUser
            }
            _userState.update {
                UserState(
                    id = localUser.userId,
                    name = name,
                    uid = uid
                )
            }
            _error.update { null }
        }
    }

    private fun resetState() {
        _userState.update { UserState() }
        _error.update { null }
    }

    fun clearError() {
        _error.update { null }
    }
}

class UserViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(application) as T
    }
}

enum class EmailResult {
    Valid,
    Empty,
    Invalid
}

enum class PasswordResult {
    Valid,
    Empty,
    Short,
    Invalid
}

fun checkEmail(email: String): EmailResult {
    if (email.isEmpty()) return EmailResult.Empty
    val pattern = Regex("^[\\w.]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$")
    return if (pattern.matches(email)) EmailResult.Valid else EmailResult.Invalid
}

fun checkPassword(password: String): PasswordResult {
    if (password.isEmpty()) return PasswordResult.Empty
    if (password.length < 6) return PasswordResult.Short
    val hasDigit = Regex("\\d+").containsMatchIn(password)
    val hasLower = Regex("[a-z]+").containsMatchIn(password)
    val hasUpper = Regex("[A-Z]+").containsMatchIn(password)
    return if (hasDigit && hasLower && hasUpper) PasswordResult.Valid else PasswordResult.Invalid
}

@Composable
fun ErrorText(error: String?, onClear: () -> Unit) {
    if (error != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = error, color = Color.Red, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onClear, modifier = Modifier.fillMaxWidth(0.5f)) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
fun userEmail(modifier: Modifier = Modifier): String {
    var email by remember { mutableStateOf("") }
    TextField(
        value = email,
        onValueChange = { email = it },
        label = { Text(stringResource(R.string.email_hint)) },
        modifier = modifier
    )
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = modifier
    )
    return passwd
}

@Composable
fun LogInSignUpButton(
    email: String,
    password: String,
    viewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Button(
        onClick = {
            val emailResult = checkEmail(email)
            val passwordResult = checkPassword(password)
            when {
                emailResult == EmailResult.Empty ->
                    viewModel._error.update { context.getString(R.string.empty_email) }
                emailResult == EmailResult.Invalid ->
                    viewModel._error.update { context.getString(R.string.invalid_email) }
                passwordResult == PasswordResult.Empty ->
                    viewModel._error.update { context.getString(R.string.empty_password) }
                passwordResult == PasswordResult.Short ->
                    viewModel._error.update { context.getString(R.string.short_password) }
                passwordResult == PasswordResult.Invalid ->
                    viewModel._error.update { context.getString(R.string.invalid_password) }
                else -> viewModel.signInOrCreate(email, password)
            }
        },
        modifier = modifier.fillMaxWidth(0.8f).height(50.dp)
    ) {
        Text(stringResource(R.string.login_button))
    }
}

@Composable
fun AskNamePage(
    viewModel: UserViewModel,
    onNavigateToHome: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ErrorText(error = error, onClear = { viewModel.clearError() })
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.name_hint)) },
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (name.isNotEmpty()) {
                    viewModel.updateName(name)
                    onNavigateToHome()
                } else {
                    viewModel._error.update { "Name cannot be empty" }
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
        ) {
            Text(stringResource(R.string.confirm_button))
        }
    }
}

@Composable
fun LoginPage(
    viewModel: UserViewModel,
    onNavigateToAskName: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val userState by viewModel.userState.collectAsState()
    val error by viewModel.error.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(userState.uid, userState.name) {
        when {
            userState.uid.isNotEmpty() && userState.name.isBlank() -> onNavigateToAskName()
            userState.uid.isNotEmpty() && userState.name.isNotBlank() -> onNavigateToHome()
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ErrorText(error = error, onClear = { viewModel.clearError() })
            Spacer(modifier = Modifier.height(16.dp))
            email = userEmail(modifier = Modifier.fillMaxWidth(0.8f))
            Spacer(modifier = Modifier.height(16.dp))
            password = userPassword(modifier = Modifier.fillMaxWidth(0.8f))
            Spacer(modifier = Modifier.height(24.dp))
            LogInSignUpButton(
                email = email,
                password = password,
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    viewModel: UserViewModel,
    onNavigateToLogin: () -> Unit
) {
    val userState by viewModel.userState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    Button(onClick = { viewModel.signOut(); onNavigateToLogin() }) {
                        Text(stringResource(R.string.logout))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { viewModel.deleteAccount(); onNavigateToLogin() }) {
                        Text(stringResource(R.string.delete_account))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome ${userState.name}!",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your UID: ${userState.uid}",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val viewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(context.applicationContext as android.app.Application)
    )

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginPage(
                viewModel = viewModel,
                onNavigateToAskName = { navController.navigate("askName") { popUpTo("login") { inclusive = true } } },
                onNavigateToHome = { navController.navigate("home") { popUpTo("login") { inclusive = true } } }
            )
        }
        composable("askName") {
            AskNamePage(
                viewModel = viewModel,
                onNavigateToHome = { navController.navigate("home") { popUpTo("askName") { inclusive = true } } }
            )
        }
        composable("home") {
            HomePage(
                viewModel = viewModel,
                onNavigateToLogin = { navController.navigate("login") { popUpTo("home") { inclusive = true } } }
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavHost()
            }
        }
    }
}