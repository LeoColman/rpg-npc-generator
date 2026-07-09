package me.kerooker.rpgnpcgenerator.repository.image

/**
 * Parameters for a single portrait render. The server (FastSD on ritalee) owns the model and
 * sampling settings; the app only supplies the prompt and the target size, which matches the
 * portrait card's aspect (512x640).
 */
data class PortraitRequest(
    val prompt: String,
    val negativePrompt: String,
    val width: Int = 512,
    val height: Int = 640
)
