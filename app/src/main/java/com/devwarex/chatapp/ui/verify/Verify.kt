package com.devwarex.chatapp.ui.verify

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.devwarex.chatapp.R


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VerifyScreen(
    modifier: Modifier = Modifier,
    viewModel:VerifyViewModel = hiltViewModel()
){
    val (sent,requestingCode,verifying,success,drop,selectedCountry,phone) = viewModel.uiState.collectAsState().value
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
            text = stringResource(id = R.string.verify_its_you),
            modifier = modifier.align(Alignment.CenterHorizontally),
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.body1
        )
        Spacer(modifier = modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.verify_message),
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(all = 32.dp),
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = modifier.height(16.dp))
        ExposedDropdownMenuBox(
            modifier = modifier.padding(end = 16.dp, top = 24.dp, bottom = 24.dp, start = 48.dp),
            expanded = false,
            onExpandedChange = { viewModel.dropDown() }
        ) {
            TextField(
                readOnly = true,
                enabled = !sent && !verifying && !success,
                value = if (selectedCountry == null) stringResource(id = R.string.select_country_message) else "${selectedCountry.flag} ${selectedCountry.name.common}",
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Country") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = drop
                    ) },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    backgroundColor = MaterialTheme.colors.background
                )
            )
            val countries = viewModel.countries.collectAsState().value
            ExposedDropdownMenu(expanded = drop, onDismissRequest = { viewModel.dropDown() }) {
                countries.forEach { country ->
                    DropdownMenuItem(onClick = {
                        viewModel.onCountrySelect(country)
                    }) {
                        Text(text = country.flag + "  " + country.name.common)
                    }
                }
            }
        }
        Row() {
            Text(
                text = if (selectedCountry == null) "ـــ" else "${selectedCountry.cca2} ${selectedCountry.idd.root}${selectedCountry.idd.suffixes[0]}",
                modifier = modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically),
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            OutlinedTextField(
                value = phone,
                onValueChange = viewModel::setPhone,
                singleLine = true,
                enabled = !sent && !verifying && !success,
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    backgroundColor = MaterialTheme.colors.background
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, start = 16.dp),
                label = { Text(text = stringResource(id = R.string.phone_title)) },
                placeholder = { Text(text = stringResource(id = R.string.phone_title)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }
        Spacer(modifier = modifier.height(16.dp))
        if (!requestingCode && !sent && !verifying && !success) {
            Button(onClick = { viewModel.onRequestCode() }, modifier = modifier.align(Alignment.CenterHorizontally)) {
                Text(text = stringResource(id = R.string.get_verify_title))
            }
        }
        if (requestingCode){
            LinearProgressIndicator(modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(all = 32.dp)
                .fillMaxSize()
            )
        }
        if (sent){
            AlertDialog(onDismissRequest = {  },
                properties = DialogProperties(dismissOnBackPress = false,dismissOnClickOutside = false),
                title = {
                    Text(text = stringResource(id = R.string.type_code_title))
                }, buttons = {
                    val code = viewModel.code.collectAsState()
                    OutlinedTextField(
                        value = code.value,
                        onValueChange = viewModel::setCode,
                        singleLine = true,
                        enabled = !verifying,
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = MaterialTheme.colors.onSurface,
                            backgroundColor = MaterialTheme.colors.background
                        ),
                        modifier = modifier
                            .padding(all = 16.dp)
                            .align(Alignment.CenterHorizontally),
                        label = { Text(text = stringResource(id = R.string.verification_code)) },
                        placeholder = { Text(text = stringResource(id = R.string.code_title)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.onVerify() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary) ,
                        modifier = modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)) {
                        Text(text = stringResource(id = R.string.verify_title))
                    }
                })
        }
        if (verifying){
            LinearProgressIndicator(modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(all = 32.dp)
                .fillMaxSize()
            )
        }

    }
}