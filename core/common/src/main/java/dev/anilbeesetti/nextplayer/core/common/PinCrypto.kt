package dev.anilbeesetti.nextplayer.core.common

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Utilities for storing a settings-lock PIN as a salted PBKDF2 hash, never in plain text.
 */
object PinCrypto {

    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    /** Generates a new random salt, base64-encoded for storage. */
    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /** Hashes [pin] with [saltBase64], returning a base64-encoded hash suitable for storage/comparison. */
    fun hash(pin: String, saltBase64: String): String {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val key = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        return Base64.encodeToString(key, Base64.NO_WRAP)
    }

    /** Verifies [pin] against a previously stored [expectedHash]/[saltBase64] pair. */
    fun verify(pin: String, saltBase64: String, expectedHash: String): Boolean {
        return hash(pin, saltBase64) == expectedHash
    }
}
