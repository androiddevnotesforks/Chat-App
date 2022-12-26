package com.devwarex.chatapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devwarex.chatapp.R
import com.devwarex.chatapp.RegistrationViewModel
import com.devwarex.chatapp.ui.theme.LightBlack

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    CustomMainLayout{
        Text(
            text = stringResource(id = R.string.app_name),
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.h3,
            modifier = modifier.wrapContentSize(),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CustomMainLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier.fillMaxWidth(),
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEach { placeable ->
                placeable.placeRelative(x = 0, y = 0)
            }
        }
    }
}


@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel = viewModel()
) {
    Column(modifier = Modifier.verticalScroll(state = ScrollState(0))) {
        Icon(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "app logo",
            modifier = Modifier
                .padding(top = 32.dp)
                .align(Alignment.CenterHorizontally)
                .size(72.dp),
            tint = MaterialTheme.colors.onSurface
        )
        Text(
            text = stringResource(id = R.string.app_name),
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.h5,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            fontWeight = FontWeight.Bold

        )
        Spacer(modifier = Modifier.padding(top = 72.dp))
        Button(
            onClick = { viewModel.signInWithGoogle() },
            elevation = ButtonDefaults.elevation(4.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.surface
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Row() {
                Image(
                    painter = painterResource(id = R.drawable.googleg_standard_color_18),
                    contentDescription = "google icon"
                )
                Text(
                    text = stringResource(id = R.string.google_sign_in_title),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.padding(top = 72.dp))
        Text(
            text = stringResource(id = R.string.or_title),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.padding(top = 72.dp))
        Button(
            onClick = { viewModel.toSignIn() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Row() {
                Icon(
                    painter = painterResource(id = R.drawable.ic_email),
                    contentDescription = "email sign in",
                    tint = MaterialTheme.colors.onBackground
                )
                Text(
                    text = stringResource(id = R.string.sign_in_title),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.padding(top = 16.dp))
        Button(
            onClick = { viewModel.toSignUp() },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.secondary
            )
        ) {
            Row() {
                Icon(
                    painter = painterResource(id = R.drawable.ic_email),
                    contentDescription = "email sign up",
                    tint = LightBlack
                )
                Text(
                    text = stringResource(id = R.string.sign_up_title),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                    color = LightBlack
                )
            }
        }
    }
}