package me.kerooker.rpgnpcgenerator.repository.image

/**
 * Points the app at the server-side renderer (stable-diffusion.cpp sd-server on ritalee, exposed
 * via caddy at npc-img.colman.com.br behind HTTP basic auth). Credentials are injected from
 * BuildConfig (set in gradle from a non-committed properties file) so they stay out of source.
 *
 * NOTE: basic-auth creds shipped in an APK are extractable; they deter casual/bot abuse, not a
 * determined attacker. Move to per-install tokens or a signed request before wide release.
 */
data class RemoteImageConfig(
    val baseUrl: String,
    val username: String,
    val password: String
) {
    val enabled: Boolean get() = baseUrl.isNotBlank() && password.isNotBlank()
}
