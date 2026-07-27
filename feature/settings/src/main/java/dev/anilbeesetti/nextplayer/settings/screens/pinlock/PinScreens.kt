package dev.anilbeesetti.nextplayer.settings.screens.pinlock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dev.anilbeesetti.nextplayer.core.ui.R

private const val MIN_PIN_LENGTH = 4

/** First-run screen: the parent sets a PIN (entered twice) that will guard Settings from then on. */
@Composable
fun CreatePinScreen(onPinCreated: (String) -> Unit) {
    var firstEntry by remember { mutableStateOf<String?>(null) }
    var pin by remember { mutableStateOf("") }
    var mismatch by remember { mutableStateOf(false) }

    fun onContinue() {
        val first = firstEntry
        when {
            first == null -> {
                firstEntry = pin
                pin = ""
            }
            pin == first -> onPinCreated(pin)
            else -> {
                mismatch = true
                firstEntry = null
                pin = ""
            }
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.surfaceContainer) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = stringResource(if (firstEntry == null) R.string.create_pin_title else R.string.confirm_pin_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            if (firstEntry == null) {
                Text(text = stringResource(R.string.create_pin_desc), style = MaterialTheme.typography.bodyMedium)
            }
            if (mismatch) {
                Text(
                    text = stringResource(R.string.confirm_pin_mismatch),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            OutlinedTextField(
                value = pin,
                onValueChange = {
                    pin = it.filter(Char::isDigit)
                    mismatch = false
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
            )
            Button(
                enabled = pin.length >= MIN_PIN_LENGTH,
                onClick = ::onContinue,
            ) {
                Text(text = stringResource(R.string.continue_action))
            }
        }
    }
}

/** Shown every time Settings is opened once a PIN is configured. */
@Composable
fun EnterPinScreen(error: String?, onSubmit: (String) -> Unit, onErrorConsumed: () -> Unit) {
    var pin by remember { mutableStateOf("") }

    Scaffold(containerColor = MaterialTheme.colorScheme.surfaceContainer) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            Text(text = stringResource(R.string.enter_pin_title), style = MaterialTheme.typography.headlineSmall)
            Text(text = stringResource(R.string.enter_pin_desc), style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = pin,
                onValueChange = {
                    pin = it.filter(Char::isDigit)
                    onErrorConsumed()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                isError = error != null,
                supportingText = error?.let { { Text(stringResource(R.string.wrong_pin)) } },
            )
            Button(
                enabled = pin.isNotEmpty(),
                onClick = { onSubmit(pin) },
            ) {
                Text(text = stringResource(R.string.unlock))
            }
        }
    }
}
