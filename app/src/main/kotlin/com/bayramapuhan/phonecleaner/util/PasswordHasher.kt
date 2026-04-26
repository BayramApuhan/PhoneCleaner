package com.bayramapuhan.phonecleaner.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256
    private const val SALT_BYTES = 16
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val SCHEME = "pbkdf2"

    fun hash(password: CharArray): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val key = derive(password, salt, ITERATIONS)
        return "$SCHEME:$ITERATIONS:${b64(salt)}:${b64(key)}"
    }

    fun verify(password: CharArray, encoded: String): Boolean {
        val parts = encoded.split(":")
        if (parts.size != 4 || parts[0] != SCHEME) return false
        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = decode(parts[2]) ?: return false
        val expected = decode(parts[3]) ?: return false
        val actual = derive(password, salt, iterations)
        return constantTimeEquals(expected, actual)
    }

    private fun derive(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, KEY_LENGTH)
        return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
    }

    private fun b64(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.NO_PADDING)

    private fun decode(value: String): ByteArray? = runCatching {
        Base64.decode(value, Base64.NO_WRAP or Base64.NO_PADDING)
    }.getOrNull()

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
        return diff == 0
    }
}
