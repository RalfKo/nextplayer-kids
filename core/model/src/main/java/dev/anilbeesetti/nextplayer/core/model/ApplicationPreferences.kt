package dev.anilbeesetti.nextplayer.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationPreferences(
    val sortBy: Sort.By = Sort.By.TITLE,
    val sortOrder: Sort.Order = Sort.Order.ASCENDING,
    val themeConfig: ThemeConfig = ThemeConfig.SYSTEM,
    val useHighContrastDarkTheme: Boolean = false,
    val useDynamicColors: Boolean = true,
    val markLastPlayedMedia: Boolean = true,
    val excludeFolders: List<String> = emptyList(),

    // Kids-mode folder whitelist: when enabled, only videos under one of `allowedFolders`
    // (or its subfolders) are ever shown, regardless of anything else on the device.
    val restrictToSelectedFolders: Boolean = false,
    val allowedFolders: List<String> = emptyList(),

    // Settings PIN lock. Only a salted hash is ever persisted, never the PIN itself.
    val settingsPinHash: String? = null,
    val settingsPinSalt: String? = null,

    val mediaViewMode: MediaViewMode = MediaViewMode.FOLDERS,
    val mediaLayoutMode: MediaLayoutMode = MediaLayoutMode.LIST,

    // Fields
    val showDurationField: Boolean = true,
    val showFolderDurationField: Boolean = true,
    val showExtensionField: Boolean = false,
    val showPathField: Boolean = true,
    val showResolutionField: Boolean = false,
    val showSizeField: Boolean = false,
    val showThumbnailField: Boolean = true,
    val showPlayedProgress: Boolean = true,

    // Thumbnail generation
    val thumbnailGenerationStrategy: ThumbnailGenerationStrategy = ThumbnailGenerationStrategy.FRAME_AT_PERCENTAGE,
    val thumbnailFramePosition: Float = DEFAULT_THUMBNAIL_FRAME_POSITION,
) {

    companion object {
        const val DEFAULT_THUMBNAIL_FRAME_POSITION = 0.33f
    }
}

/**
 * Whether [path] should ever be visible to the media library, combining the kids-mode
 * whitelist (if enabled) with the ordinary exclude list. Both checks are path-prefix based,
 * so a folder decision also applies to everything nested inside it.
 */
fun ApplicationPreferences.isFolderVisible(path: String): Boolean {
    if (restrictToSelectedFolders) {
        val isAllowed = allowedFolders.any { root -> path == root || path.startsWith("$root/") }
        if (!isAllowed) return false
    }
    return excludeFolders.none { excluded -> path == excluded || path.startsWith("$excluded/") }
}
