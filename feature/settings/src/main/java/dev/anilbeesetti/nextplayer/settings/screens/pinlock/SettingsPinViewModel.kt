package dev.anilbeesetti.nextplayer.settings.screens.pinlock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anilbeesetti.nextplayer.core.common.PinCrypto
import dev.anilbeesetti.nextplayer.core.data.repository.PreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Whether Settings currently requires a PIN, is unlocked, or has no PIN configured yet. */
sealed interface PinGateState {
    data object Loading : PinGateState
    data object NoPinConfigured : PinGateState
    data object Locked : PinGateState
    data object ChangingPin : PinGateState
    data object Unlocked : PinGateState
}

@HiltViewModel
class SettingsPinViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val stateInternal = MutableStateFlow<PinGateState>(PinGateState.Loading)
    val state: StateFlow<PinGateState> = stateInternal.asStateFlow()

    private val errorInternal = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = errorInternal.asStateFlow()

    init {
        val prefs = preferencesRepository.applicationPreferences.value
        stateInternal.value = if (prefs.settingsPinHash == null) {
            PinGateState.NoPinConfigured
        } else {
            PinGateState.Locked
        }
    }

    fun createPin(pin: String) {
        viewModelScope.launch {
            val salt = PinCrypto.newSalt()
            val hash = PinCrypto.hash(pin, salt)
            preferencesRepository.updateApplicationPreferences {
                it.copy(settingsPinHash = hash, settingsPinSalt = salt)
            }
            stateInternal.value = PinGateState.Unlocked
        }
    }

    fun attemptUnlock(pin: String) {
        val prefs = preferencesRepository.applicationPreferences.value
        val salt = prefs.settingsPinSalt
        val hash = prefs.settingsPinHash
        if (salt != null && hash != null && PinCrypto.verify(pin, salt, hash)) {
            errorInternal.value = null
            stateInternal.value = PinGateState.Unlocked
        } else {
            errorInternal.value = "error"
        }
    }

    fun clearError() {
        errorInternal.value = null
    }

    fun startChangePin() {
        stateInternal.value = PinGateState.ChangingPin
    }

    fun cancelChangePin() {
        stateInternal.value = PinGateState.Locked
    }

    /** Checks [pin] against the currently stored PIN without changing [state]. */
    fun verifyCurrentPinForChange(pin: String): Boolean {
        val prefs = preferencesRepository.applicationPreferences.value
        val salt = prefs.settingsPinSalt
        val hash = prefs.settingsPinHash
        return salt != null && hash != null && PinCrypto.verify(pin, salt, hash)
    }
}
