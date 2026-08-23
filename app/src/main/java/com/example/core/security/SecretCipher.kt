package com.example.core.security

/** Thrown when encrypting or decrypting a secret fails. Never swallow into an empty string. */
class SecretCipherException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Seam for secret encryption. Production impl: [KeystoreCipher].
 * Contract (CONTEXT.md invariant): a failed encryption must never be persisted as "".
 */
interface SecretCipher {
    /** @throws SecretCipherException if encryption fails. */
    fun encrypt(plainText: String): String

    /** @throws SecretCipherException if decryption fails. */
    fun decrypt(cipherText: String): String
}
