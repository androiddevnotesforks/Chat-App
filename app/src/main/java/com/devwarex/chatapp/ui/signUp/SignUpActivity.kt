package com.devwarex.chatapp.ui.signUp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.devwarex.chatapp.R
import com.devwarex.chatapp.ui.MainActivity
import com.devwarex.chatapp.ui.conversation.ConversationActivity
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.devwarex.chatapp.ui.theme.DarkBackground
import com.devwarex.chatapp.ui.theme.LightBlue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SignUpScreen()
                }
            }
        }
        val viewModel: SignUpViewModel by viewModels()
        viewModel.isSucceed.observe(this){
            if (it) {
                val homeIntent = Intent(this, MainActivity::class.java)
                startActivity(homeIntent)
                finish()
            }
        }
    }
}


@Composable
fun SignUpScreen(modifier: Modifier = Modifier){
    val viewModel = hiltViewModel<SignUpViewModel>()
    val (name,email,password,confirmPassword,isLoading,isSucceed,errors) = viewModel.uiState.collectAsState().value
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(state = ScrollState(0))
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            color = MaterialTheme.colors.primary,
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(id = R.string.sign_up_title),
            modifier = modifier.align(Alignment.CenterHorizontally),
            color = MaterialTheme.colors.onSurface
        )

        Spacer(modifier = modifier.height(32.dp))
        NameEditText(
            modifier = modifier.align(Alignment.CenterHorizontally),
            viewModel = viewModel,
            name = name,
            enable = !isSucceed,
            nameError = errors.name
        )
        Spacer(modifier = modifier.height(16.dp))
        EmailEditText(
            modifier = modifier,
            viewModel = viewModel,
            email = email,
            enable = !isSucceed,
            emailError = errors.email
        )
        Spacer(modifier = modifier.height(16.dp))
        PasswordEditText(
            modifier = modifier,
            viewModel = viewModel,
            password = password,
            enable = !isSucceed,
            passwordError = errors.password
        )
        Spacer(modifier = modifier.height(16.dp))
        ConfirmPasswordEditText(
            modifier = modifier,
            viewModel = viewModel,
            confirmPassword = confirmPassword,
            enable = !isSucceed,
            error = errors.confirmPassword
        )
        Spacer(modifier = Modifier.height(48.dp))
        SignUpLoadingState(
            isLoading = isLoading,
            viewModel = viewModel,
            enable = !isSucceed,
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp, bottom = 16.dp)
        )
    }
}


@Composable
fun SignUpLoadingState(
    isLoading: Boolean,
    modifier: Modifier,
    viewModel: SignUpViewModel,
    enable: Boolean
){
    if (enable) {
        if (isLoading) {
            CircularProgressIndicator(modifier = modifier)
        } else {
            Button(
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.secondary
                ),
                onClick = { viewModel.signUp() },
                modifier = modifier
                    .fillMaxWidth()
                    .requiredHeight(48.dp)
                    .padding(end = 24.dp, start = 24.dp)
            ) {
                Text(text = stringResource(id = R.string.sign_up_title))
            }
        }
    }else{
        Snackbar(
            backgroundColor = LightBlue
        ) {
            Text(text = stringResource(id = R.string.success_sign_up),color = DarkBackground)
        }
    }
}
@Composable
fun NameEditText(
    modifier: Modifier,
    viewModel: SignUpViewModel,
    name: String,
    nameError: ErrorsState,
    enable: Boolean
){
    Column {
        OutlinedTextField(
            value = name,
            onValueChange = viewModel::updateName,
            singleLine = true,
            enabled = enable,
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colors.onSurface,
                backgroundColor = MaterialTheme.colors.background
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 16.dp, start = 16.dp),
            label = { Text(text = stringResource(id = R.string.name_title)) },
            placeholder = { Text(text = stringResource(id = R.string.name_title)) },
            isError = nameError != ErrorsState.NONE
        )
        if (nameError != ErrorsState.NONE) {
            Text(
                text = stringResource(id = when(nameError){
                    ErrorsState.EMPTY -> R.string.empty_message
                    ErrorsState.INVALID_NAME -> R.string.invalid_name_message
                    else -> R.string.empty
                }),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}

@Composable
fun EmailEditText(
    modifier: Modifier,
    viewModel: SignUpViewModel,
    email: String,
    emailError: ErrorsState,
    enable: Boolean
){
    Column {
        OutlinedTextField(
            value = email,
            onValueChange = viewModel::updateEmail,
            singleLine = true,
            enabled = enable,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colors.onSurface,
                backgroundColor = MaterialTheme.colors.background
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 16.dp, start = 16.dp),
            label = { Text(text = stringResource(id = R.string.email_title)) },
            placeholder = { Text(text = stringResource(id = R.string.email_title)) },
            isError = emailError != ErrorsState.NONE
        )
        if (emailError != ErrorsState.NONE) {
            Text(
                text = stringResource(id = when(emailError){
                    ErrorsState.EMPTY -> R.string.empty_message
                    ErrorsState.INVALID_EMAIL -> R.string.invalid_email_message
                    ErrorsState.EMAIL_EXIST -> R.string.email_exist_message
                    else -> R.string.empty
                }),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}

@Composable
fun PasswordEditText(
    modifier: Modifier,
    viewModel: SignUpViewModel,
    password: String,
    passwordError: ErrorsState,
    enable: Boolean
){

    Column {
        OutlinedTextField(
            value = password,
            onValueChange = viewModel::updatePassword,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = enable,
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colors.onSurface,
                backgroundColor = MaterialTheme.colors.background
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 16.dp, start = 16.dp),
            label = { Text(text = stringResource(id = R.string.password_title)) },
            placeholder = { Text(text = stringResource(id = R.string.password_title)) },
            isError = passwordError != ErrorsState.NONE
        )
        if (passwordError != ErrorsState.NONE) {
            Text(
                text = stringResource(id = when(passwordError){
                    ErrorsState.EMPTY -> R.string.empty_message
                    ErrorsState.WEAK_PASSWORD -> R.string.password_weak_message
                    else -> R.string.empty
                }),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}

@Composable
fun ConfirmPasswordEditText(
    modifier: Modifier,
    viewModel: SignUpViewModel,
    confirmPassword: String,
    error: ErrorsState,
    enable: Boolean
){
    Column {
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = viewModel::updateConfirmPassword,
            singleLine = true,
            enabled = enable,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colors.onSurface,
                backgroundColor = MaterialTheme.colors.background
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 16.dp, start = 16.dp),
            label = { Text(text = stringResource(id = R.string.confirm_password_title)) },
            placeholder = { Text(text = stringResource(id = R.string.confirm_password_title)) },
            isError = error != ErrorsState.NONE
        )
        if ( error != ErrorsState.NONE) {
            Text(
                text = stringResource(id = when(error){
                    ErrorsState.EMPTY -> R.string.empty_message
                    ErrorsState.NOT_MATCH_PASSWORD -> R.string.password_not_same_message
                    else -> R.string.empty
                }),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChatAppTheme {
        SignUpScreen()
    }
}