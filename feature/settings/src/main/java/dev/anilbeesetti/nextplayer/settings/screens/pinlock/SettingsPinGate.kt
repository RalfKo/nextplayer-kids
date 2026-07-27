package dev.anilbeesetti.nextplayer.settings.screens.pinlock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Wraps [content] (the real Settings UI) so it can only ever be composed once the correct PIN
 * has been entered for this visit. Leaving Settings and re-entering always asks again, since
 * this state lives only in [SettingsPinViewModel]'s (per-navigation-entry) view model store.
 */
@Composable
fun SettingsPinGate(content: @Composable () -> Unit) {
    val viewModel: SettingsPinViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    when (state) {
        PinGateState.Loading -> Unit
        PinGateState.NoPinConfigured -> CreatePinScreen(onPinCreated = viewModel::createPin)
        PinGateState.Locked -> EnterPinScreen(
            error = error,
            onSubmit = viewModel::attemptUnlock,
            onErrorConsumed = viewModel::clearError,
        )
        PinGateState.Unlocked -> content()
    }
}
