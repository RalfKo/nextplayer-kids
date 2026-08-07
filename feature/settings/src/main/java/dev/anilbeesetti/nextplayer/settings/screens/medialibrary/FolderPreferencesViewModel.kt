package dev.anilbeesetti.nextplayer.settings.screens.medialibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anilbeesetti.nextplayer.core.data.repository.MediaRepository
import dev.anilbeesetti.nextplayer.core.data.repository.PreferencesRepository
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.Folder
import dev.anilbeesetti.nextplayer.core.ui.base.DataState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FolderPreferencesViewModel @Inject constructor(
    mediaRepository: MediaRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val uiStateInternal = MutableStateFlow(
        FolderPreferencesUiState(
            preferences = preferencesRepository.applicationPreferences.value,
        ),
    )
    val uiState: StateFlow<FolderPreferencesUiState> = uiStateInternal.asStateFlow()

    init {
        viewModelScope.launch {
            mediaRepository.observeFolders().collect {
                uiStateInternal.update { currentState ->
                    currentState.copy(foldersDataState = DataState.Success(it))
                }
            }
        }

        viewModelScope.launch {
            preferencesRepository.applicationPreferences.collect { preferences ->
                uiStateInternal.update { currentState ->
                    currentState.copy(preferences = preferences)
                }
            }
        }
    }

    fun onEvent(event: FolderPreferencesUiEvent) {
        when (event) {
            is FolderPreferencesUiEvent.UpdateExcludeList -> updateExcludeList(event.path)
            is FolderPreferencesUiEvent.UpdateAllowList -> updateAllowList(event.path)
            is FolderPreferencesUiEvent.ToggleRestrictMode -> toggleRestrictMode()
            is FolderPreferencesUiEvent.AddAllowedFolder -> addAllowedFolder(event.path)
        }
    }

    /**
     * Adds [path] to the allow list, if not already present. Unlike [updateAllowList] this never
     * removes it, so it's safe to call from a folder picker where re-picking the same folder
     * shouldn't toggle it off. Since [dev.anilbeesetti.nextplayer.core.model.isFolderVisible]
     * matches allowed folders by path prefix, any folder added here also covers all of its
     * current and future subfolders.
     */
    private fun addAllowedFolder(path: String) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(allowedFolders = if (path in it.allowedFolders) it.allowedFolders else it.allowedFolders + path)
            }
        }
    }

    private fun updateExcludeList(path: String) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(
                    excludeFolders = if (path in it.excludeFolders) {
                        it.excludeFolders - path
                    } else {
                        it.excludeFolders + path
                    },
                )
            }
        }
    }

    private fun updateAllowList(path: String) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(
                    allowedFolders = if (path in it.allowedFolders) {
                        it.allowedFolders - path
                    } else {
                        it.allowedFolders + path
                    },
                )
            }
        }
    }

    private fun toggleRestrictMode() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(restrictToSelectedFolders = !it.restrictToSelectedFolders)
            }
        }
    }
}

data class FolderPreferencesUiState(
    val foldersDataState: DataState<List<Folder>> = DataState.Loading,
    val preferences: ApplicationPreferences = ApplicationPreferences(),
)

sealed interface FolderPreferencesUiEvent {
    data class UpdateExcludeList(val path: String) : FolderPreferencesUiEvent
    data class UpdateAllowList(val path: String) : FolderPreferencesUiEvent
    data object ToggleRestrictMode : FolderPreferencesUiEvent
    data class AddAllowedFolder(val path: String) : FolderPreferencesUiEvent
}
