package com.devwarex.chatapp.ui.signUp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.runtime.remember
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
import com.devwarex.chatapp.MainActivity
import com.devwarex.chatapp.R
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.devwarex.chatapp.viewModels.SignUpViewModel
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
            viewModel = viewModel
        )
        Spacer(modifier = modifier.height(16.dp))
        EmailEditText(modifier = modifier, viewModel = viewModel)
        Spacer(modifier = modifier.height(16.dp))
        PasswordEditText(modifier = modifier, viewModel = viewModel)
        Spacer(modifier = modifier.height(16.dp))
        ConfirmPasswordEditText(modifier = modifier, viewModel = viewModel)
        Spacer(modifier = Modifier.height(48.dp))
        val isLoading = remember {
            viewModel.isLoading
        }
        val loading = isLoading.collectAsState().value
        SignUpLoadingState(
            isLoading = loading,
            viewModel = viewModel,
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp, bottom = 16.dp)
        )
    }
}


@Composable
fun SignUpLoadingState(isLoading: Boolean,modifier: Modifier,viewModel: SignUpViewModel){
    if (isLoading) {
       CircularProgressIndicator(modifier = modifier)
    }else{
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
}
@Composable
fun NameEditText(modifier: Modifier,viewModel: SignUpViewModel){
    val name = viewModel.name.collectAsState()
    val message = viewModel.nameMessage.collectAsState().value
    Column {
        OutlinedTextField(
            value = name.value,
            onValueChange = viewModel::updateName,
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colors.onSurface,
                backgroundColor = MaterialTheme.colors.background
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 16.dp, start = 16.dp),
            label = { Text(text = stringResource(id = R.string.name_title)) },
            placeholder = { Text(text = stringResource(id = R.string.name_title)) },
            isError = message != R.string.empty
        )
        if (message != R.string.empty) {
            Text(
                text = stringResource(id = message),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}

@Composable
fun EmailEditText(modifier: Modifier,viewModel: SignUpViewModel){
    val email = viewModel.email.collectAsState()
    val message = viewModel.emailMessage.collectAsState().value
    Column {
        OutlinedTextField(
            value = email.value,
            onValueChange = viewModel::updateEmail,
            singleLine = true,
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
            isError = message != R.string.empty
        )
        if (message != R.string.empty) {
            Text(
                text = stringResource(id = message),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}

@Composable
fun PasswordEditText(modifier: Modifier,viewModel: SignUpViewModel){
    val password = viewModel.password.collectAsState()
    val message = viewModel.passwordMessage.collectAsState().value
    Column {
        OutlinedTextField(
            value = password.value,
            onValueChange = viewModel::updatePassword,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
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
            isError = message != R.string.empty
        )
        if (message != R.string.empty) {
            Text(
                text = stringResource(id = message),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}

@Composable
fun ConfirmPasswordEditText(modifier: Modifier,viewModel: SignUpViewModel){
    val password = viewModel.confirmPassword.collectAsState()
    val message = viewModel.confirmPasswordMessage.collectAsState().value
    Column {
        OutlinedTextField(
            value = password.value,
            onValueChange = viewModel::updateConfirmPassword,
            singleLine = true,
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
            isError = message != R.string.empty
        )
        if (message != R.string.empty) {
            Text(
                text = stringResource(id = message),
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